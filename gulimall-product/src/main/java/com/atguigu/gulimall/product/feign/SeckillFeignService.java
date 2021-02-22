package com.atguigu.gulimall.product.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.feign.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 孟享广
 * @date 2021-02-21 10:51 上午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
//feign sentinel 熔断保护 fallback =表示出错了 回调哪个？
@FeignClient(value = "gulimall-seckill", fallback = SeckillFeignServiceFallBack.class)//这个远程服务
public interface SeckillFeignService {

    /**
     * 来自远程服务gulimall-product
     * 获取当前sku的秒杀预告信息
     */
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
