package com.atguigu.gulimall.seckill.Service.impl;

import com.atguigu.gulimall.seckill.Service.SeckillService;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 孟享广
 * @date 2021-02-20 11:18 上午
 * @description
 */
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1 扫描需要参与秒杀的活动
    }
}
