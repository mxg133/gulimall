package com.atguigu.gulimall.seckill.Service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.Service.SeckillService;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 孟享广
 * @date 2021-02-20 11:18 上午
 * @description
 */
public class SeckillServiceImpl implements SeckillService {

    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1 扫描需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaysSession();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSkus> seckillSessionWithSkuses = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //缓存到redis
            //1 缓存活动信息
            saveSessionInfos(seckillSessionWithSkuses);
            //2 缓存商品信息
            saveSessionSkuInfos(seckillSessionWithSkuses);
        }

    }

    /**
     * 1 缓存活动信息
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> seckillSessionWithSkuses) {

        seckillSessionWithSkuses.stream().forEach((seckillSessionsWithSkus)->{
            Long startTime = seckillSessionsWithSkus.getStartTime().getTime();
            Long endTime = seckillSessionsWithSkus.getEndTime().getTime();

            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            List<String> ids = seckillSessionsWithSkus.getSeckillSkuRelationEntities().stream().map((seckillSkuRelationEntity)->{
                return seckillSkuRelationEntity.getId().toString();
            }).collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(key, ids);
        });
    }

    /**
     * 2 缓存商品信息
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> seckillSessionWithSkuses) {

        seckillSessionWithSkuses.stream().forEach((seckillSessionsWithSkus)->{
            //准备hash
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            seckillSessionsWithSkus.getSeckillSkuRelationEntities().stream().forEach(seckillSkuVo -> {

                //缓存商品
                SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();

                //sku的基本数据

                //sku的秒杀信息
                BeanUtils.copyProperties(seckillSkuVo, seckillSkuRedisTo);


                String s = JSON.toJSONString(seckillSkuRedisTo);
                ops.put(seckillSkuVo.getId(), s);


            });
        });
    }
}
