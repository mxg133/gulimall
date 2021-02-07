package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 孟享广
 * @date 2021-02-07 1:49 下午
 * @description 订单确认页需要的数据
 */
@Data
public class OrderConfirmVo {

    //收货地址列表
    List<MemberAddressVo> address;

    //所有选中的购物项
    List<OrderItemVo> items;

    //发票....

    //优惠券 积分
    private Integer integration;

    //订单总额 需要计算
    BigDecimal total;

    //应付价格 需要计算
    BigDecimal payPrice;
}
