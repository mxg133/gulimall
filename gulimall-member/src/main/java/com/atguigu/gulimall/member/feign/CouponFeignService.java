package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 孟享广
 * @date 2020-12-16 3:19 下午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-coupon")//这个远程服务
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
