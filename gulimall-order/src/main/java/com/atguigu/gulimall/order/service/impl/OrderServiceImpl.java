package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

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

    /**
     * 提交订单 去支付
     *
     * @Transactional 是一种本地事物，在分布式系统中，只能控制住自己的回滚，控制不了其他服务的回滚
     * 分布式事物 最大的原因是 网络问题+分布式机器。
     * (isolation = Isolation.REPEATABLE_READ) MySql默认隔离级别 - 可重复读
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        //共享前端页面传过来的vo
        confirmVoThreadLocal.set(vo);

        //要返回到大对象
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        //登录的用户
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        responseVo.setCode(0);
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
            //TODO 3 保存订单
            OrderCreatTo orderCreatTo = creatOrder();
            BigDecimal payAmount = orderCreatTo.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            //金额对比
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //保存到数据库
                saveOrder(orderCreatTo);
                //库存锁定 只要有异常就回本订单数据
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(orderCreatTo.getOrder().getOrderSn());
                List<OrderItemVo> collect = orderCreatTo.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                //TODO 4 远程锁库存 非常严重
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    //锁成功
                    responseVo.setOrder(orderCreatTo.getOrder());
                    //TODO 5 远程扣减积分
                    //库存成功了，但是网络原因超时了，订单回滚，库存不回滚
//                    int i = 1 / 0;//模拟积分系统异常
                    return responseVo;
                } else {
                    //锁定失败
                    String msg1 = (String) r.get("msg");
                    throw new NoStockException(msg1);
                }
            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

    //保存到数据库
    private void saveOrder(OrderCreatTo orderCreatTo) {

        //保存第1个属性到数据库
        OrderEntity order = orderCreatTo.getOrder();
        order.setModifyTime(new Date());
        this.save(order);
        //保存第2个属性到数据库
        List<OrderItemEntity> orderItems = orderCreatTo.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreatTo creatOrder() {

        OrderCreatTo orderCreatTo = new OrderCreatTo();

        //orderCreatTo 第1个大属性OrderEntity order
        //生成一个订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrderEntity(orderSn);
        orderCreatTo.setOrder(order);

        //orderCreatTo 第2个大属性List<OrderItemEntity> items
        List<OrderItemEntity> orderItems = buildList_OrderItemEntity(orderSn);
        orderCreatTo.setOrderItems(orderItems);

        //orderCreatTo 第3个大属性BigDecimal payPrice;
        computePrice(order, orderItems);
        //orderCreatTo 第4个大属性BigDecimal fare;
        return orderCreatTo;
    }

    private OrderEntity buildOrderEntity(String orderSn) {

        OrderEntity order = new OrderEntity();
        order.setOrderSn(orderSn);
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        order.setMemberId(memberResVo.getId());
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
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        order.setAutoConfirmDay(7);
        return order;
    }

    private List<OrderItemEntity> buildList_OrderItemEntity(String orderSn) {

        //这是最后一次确定每一个购物项的价格了
        List<OrderItemVo> orderItemVos = cartFeignService.currentUserItems();
        if (orderItemVos != null && orderItemVos.size() > 0) {
            List<OrderItemEntity> collect = orderItemVos.stream().map((item) -> {
                OrderItemEntity orderItemEntity = buildOrderItemEntity(item);
                //OrderItemEntity的1个属性 订单号
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private OrderItemEntity buildOrderItemEntity(OrderItemVo item) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //OrderItemEntity的1个属性 spu
        Long skuId = item.getSkuId();
        R r = productFeignService.getSpuInfoBuSkuId(skuId);
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        //OrderItemEntity的1个属性 sku
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        //集合转数组
        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(item.getCount());
        //OrderItemEntity的1个属性 优惠信息
        //OrderItemEntity的1个属性 积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        //OrderItemEntity的1个属性 金额
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal orign = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract = orign.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }

    private void computePrice(OrderEntity order, List<OrderItemEntity> orderItems) {

        //总价格
        BigDecimal total = new BigDecimal("0.0");
        //优惠卷
        BigDecimal coupon = new BigDecimal("0.0");
        //积分
        BigDecimal interation = new BigDecimal("0.0");
        //打折
        BigDecimal promotion = new BigDecimal("0.0");
        //赠送积分
        BigDecimal gift = new BigDecimal("0.0");
        //赠送成长值
        BigDecimal growth = new BigDecimal("0.0");
        //订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItems) {
            coupon = coupon.add(orderItem.getCouponAmount());
            interation = interation.add(orderItem.getIntegrationAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            total = total.add(orderItem.getRealAmount());
            gift = gift.add(new BigDecimal(orderItem.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(orderItem.getGiftGrowth().toString()));
        }
        //1、订单价格相关
        order.setTotalAmount(total);
        //应付金额 + 运费金额
        order.setPayAmount(total.add(order.getFreightAmount()));
        //优惠信息
        order.setPromotionAmount(promotion);
        order.setIntegrationAmount(interation);
        order.setCouponAmount(coupon);
        //设置积分信息
        order.setGrowth(growth.intValue());
        order.setIntegration(gift.intValue());
        //未删除
        order.setDeleteStatus(0);
    }
}