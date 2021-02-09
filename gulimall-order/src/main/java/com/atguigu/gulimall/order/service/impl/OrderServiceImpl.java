package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    //共享前端页面传过来的vo
    ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 去结算
     * 给订单确认ye返回数据
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        //要返回的大对象
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        //获取用户
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

        //Feign + 异步任务 需要共享RequestAttributes 每个任务都要setRequestAttributes()
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都要共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //1 远程查询大对象的第一个属性 收货地址列表
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResVo.getId());
            confirmVo.setAddress(address);
            //Feign在远程调用之前要构造请求，调用很多的拦截器
        }, executor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都要共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2 远程查询大对象的第二个属性 所有购物项
            List<OrderItemVo> items = cartFeignService.currentUserItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> ids = items.stream().map((item) -> {
                return item.getSkuId();
            }).collect(Collectors.toList());
            //远程查看库存
            R r = wareFeignService.getSkuHasStock(ids);
            List<SkuStockVo> skuStockVos = r.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (skuStockVos != null) {
                Map<Long, Boolean> booleanMap = skuStockVos.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(booleanMap);
            }
        }, executor);

        //3 远程查询用户积分
        Integer integration = memberResVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4 其他的数据自动计算

        //TODO 5 防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //给服务器一个 并指定过期时间
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId(), token, 30, TimeUnit.MINUTES);
        //给页面一个
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        //共享前端页面传过来的vo
        confirmVoThreadLocal.set(vo);

        //要返回到大对象
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        //登录的用户
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        //1、首先验证令牌
        //0失败 - 1成功 ｜ 不存在0 存在 删除？1：0
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        //原子验证令牌 和 删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()),
                orderToken);
        if (result == 0) {
            //令牌验证失败 非0失败码
            responseVo.setCode(1);
            return responseVo;
        } else {
            //令牌验证成功 -> 执行业务代码
            //下单 去创建订单 验证令牌 验证价格 锁库存
            OrderCreatTo orderCreatTo = creatOrder();

            return responseVo;
        }
    }

    private OrderCreatTo creatOrder() {


        OrderCreatTo orderCreatTo = new OrderCreatTo();

        //orderCreatTo 第1个大属性OrderEntity order
        //生成一个订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrderEntity(orderSn);
        orderCreatTo.setOrder(order);

        //orderCreatTo 第2个大属性List<OrderItemEntity> items
        List<OrderItemEntity> orderItems = buildList_OrderItemEntity();
        orderCreatTo.setItems(orderItems);


        //orderCreatTo 第3个大属性BigDecimal payPrice;
        //orderCreatTo 第4个大属性BigDecimal fare;
        return orderCreatTo;
    }

    private OrderEntity buildOrderEntity(String orderSn) {

        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        //共享前端页面传过来的vo
        OrderSubmitVo vo = confirmVoThreadLocal.get();
        //获取收货地址
        R r = wareFeignService.getFare(vo.getAddrId());
        FareVo fareVo = r.getData(new TypeReference<FareVo>() {
        });
        order.setFreightAmount(fareVo.getFare());
        order.setReceiverCity(fareVo.getAddress().getCity());
        order.setReceiverDetailAddress(fareVo.getAddress().getDetailAddress());
        order.setReceiverName(fareVo.getAddress().getName());
        order.setReceiverPhone(fareVo.getAddress().getPhone());
        order.setReceiverPostCode(fareVo.getAddress().getPostCode());
        order.setReceiverProvince(fareVo.getAddress().getProvince());
        order.setReceiverRegion(fareVo.getAddress().getRegion());
        return order;
    }

    private List<OrderItemEntity> buildList_OrderItemEntity() {

        List<OrderItemVo> orderItemVos = cartFeignService.currentUserItems();
        if (orderItemVos != null && orderItemVos.size() > 0) {
            List<OrderItemEntity> collect = orderItemVos.stream().map((item) -> {
                return buildOrderItemEntity(item);
            }).collect(Collectors.toList());
        }
        return null;
    }

    private OrderItemEntity buildOrderItemEntity(OrderItemVo item) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        return orderItemEntity;
    }


}