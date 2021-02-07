package com.atguigu.gulimall.order.service.impl;

import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

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
    public OrderConfirmVo confirmOrder() {

        //要返回的大对象
        OrderConfirmVo confirmVo = new OrderConfirmVo();

        //获取用户
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

        //1 远程查询大对象的第一个属性 收货地址列表
        List<MemberAddressVo> address = memberFeignService.getAddress(memberResVo.getId());
        confirmVo.setAddress(address);

        //2 远程查询大对象的第二个属性 所有购物项
        List<OrderItemVo> items = cartFeignService.currentUserItems();
        confirmVo.setItems(items);

        //3 远程查询用户积分
        Integer integration = memberResVo.getIntegration();
        confirmVo.setIntegration(integration);

        //4 其他的数据自动计算

        //TODO 5 防重令牌

        return confirmVo;
    }

}