package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 孟享广
 * @date 2021-02-08 4:58 下午
 * @description
 */
@Data
public class FareVo {

    private MemberAddressVo address;
    private BigDecimal fare;
}
