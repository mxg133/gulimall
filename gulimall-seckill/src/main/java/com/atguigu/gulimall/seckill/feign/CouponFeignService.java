package com.atguigu.gulimall.seckill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 孟享广
 * @date 2021-02-20 11:25 上午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-coupon")//这个远程服务
public interface CouponFeignService {

    /**
     * 给远程服务gulimall-seckill调用
     * 扫描需要参与秒杀的活动
     */
    @GetMapping("/coupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();
}
