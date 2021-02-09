package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 孟享广
 * @date 2021-02-09 6:08 下午
 * @description
 */
@Data
public class WareSkuLockVo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 需要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;
}
