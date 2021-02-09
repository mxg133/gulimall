package com.atguigu.gulimall.ware.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 孟享广
 * @date 2021-02-09 9:09 下午
 * @description
 */
public class NoStockException extends RuntimeException {

    @Getter
    @Setter
    private Long SkuId;

    public NoStockException(Long SkuId) {
        super(SkuId + ":没有足够的库存");
    }
}
