package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author 孟享广
 * @date 2021-02-09 6:25 下午
 * @description
 */
@Data
public class LockStockResultVo {

    private Long skuId;
    private Integer num;
    /**
     * 是否成功
     */
    private boolean locked;
}
