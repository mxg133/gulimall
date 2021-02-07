package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

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
        }, executor);

        //3 远程查询用户积分
        Integer integration = memberResVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4 其他的数据自动计算

        //TODO 5 防重令牌

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;
    }

}