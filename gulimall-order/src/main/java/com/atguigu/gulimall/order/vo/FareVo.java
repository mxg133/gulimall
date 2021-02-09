package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 孟享广
 * @date 2021-02-09 3:15 下午
 * @description
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
