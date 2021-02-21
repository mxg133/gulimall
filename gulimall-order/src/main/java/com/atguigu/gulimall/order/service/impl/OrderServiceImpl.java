package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;

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
    //分布式事务 全局事务
    //@GlobalTransactional 不用
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
                /**
                 * TODO 4 远程锁库存 非常严重
                 * 库存成功了，但是网络原因超时了，订单可以回滚，库存不能回滚
                 * 为了保证高并发，库存需要自己回滚。 这样可以采用发消息给库存服务
                 * 库存服务本身也可以使用自动解锁模式 使用消息队列完成  使用延时队列
                 */
                R r = wareFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    //锁成功
                    responseVo.setOrder(orderCreatTo.getOrder());
                    //TODO 5 远程扣减积分
                    //库存成功了，但是网络原因超时了，订单回滚，库存不回滚
//                    int i = 1 / 0;//模拟积分系统异常
                    //TODO 订单创建成功，发消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderCreatTo.getOrder());
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

    /**
     * 按照订单号查询订单
     */
    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {

        OrderEntity entity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return entity;
    }

    /**
     * 超过30分钟，关闭订单
     */
    @Override
    public void closeOrder(OrderEntity entity) {

        //先来查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        //需要关单的状态是：代付款 0
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //关单
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(entity.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrder);
            //发给MQ一个
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
//            try {
//                //TODO 保证消息100%发送出去，每一个消息都做好日志记录 (给数据库保存每一个消息的详细信息)
//                //TODO 定期扫描数据库 将失败的消息再发送一遍
//                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.order", orderTo);
//            } catch (Exception e) {
//                //TODO 将没发送出去的想消息进行重复发送 while
//            }
        }
    }

    /**
     * 获取当前订单的支付信息 PayVo
     */
    @Override
    public PayVo getPayOrder(String orderSn) {

        //要返回的大对象
        PayVo payVo = new PayVo();

        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));

        //大对象属性1：设置订单的备注
        payVo.setBody(orderItemEntities.get(0).getSkuAttrsVals());
        //大对象属性2：订单号
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        //大对象属性3：订单的主题
        payVo.setSubject("谷粒商城" + orderItemEntities.get(0).getSkuName());
        //大对象属性4：订单的金额 小数点后2位+向上取值
        BigDecimal payNum = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(payNum.toString());

        //返回给前端这个大对象
        return payVo;
    }

    /**
     * 给远程服务使用的
     * 查询当前登录用户的所有订单详情数据（分页）
     */
    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();
        //降序排列
        wrapper.eq("member_id", memberResVo.getId()).orderByDesc("id");
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                wrapper
        );

        List<OrderEntity> orderEntities = page.getRecords().stream().map((orderEntity) -> {
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderEntity.getOrderSn()));
            orderEntity.setOrderItemEntities(orderItemEntities);
            return orderEntity;
        }).collect(Collectors.toList());

        //重新设置返回数据
        page.setRecords(orderEntities);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝返回的数据
     * <p>
     * 只要我们收到了，支付宝给我们的一步的通知，告诉我订单支付成功
     * 返回success，支付宝就再也不通知
     */
    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {

        //1.保存交易流水这个对象 PaymentInfoEntity
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());//修改数据库为唯一属性
        paymentInfoEntity.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);

        //2。修改订单状态
        if (payAsyncVo.getTrade_status().equals("TRADE_SUCCESS") || payAsyncVo.getTrade_status().equals("TRADE_FINISHED")) {
            //支付成功
            String outTradeNo = payAsyncVo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    @Override
    public void creatSeckillOrder(SeckillOrderTo seckillOrderTo) {

        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal("" + seckillOrderTo.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        //TODO 保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(seckillOrderTo.getNum());

        //TODO 获取当前Sku相信信息
//        productFeignService.getSpuInfoBuSkuId()

        orderItemService.save(orderItemEntity);
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