package com.atguigu.gulimall.seckill.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author 孟享广
 * @date 2021-02-20 11:25 上午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-coupon")//这个远程服务
public interface CouponFeignService {


}
