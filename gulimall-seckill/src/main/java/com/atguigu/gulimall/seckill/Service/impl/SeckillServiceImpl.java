package com.atguigu.gulimall.seckill.Service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.Service.SeckillService;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author 孟享广
 * @date 2021-02-20 11:18 上午
 * @description
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    //活动信息
    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    //sku信息
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";
    //高平发
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+随机码

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

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

        seckillSessionWithSkuses.stream().forEach((seckillSessionsWithSkus) -> {
            Long startTime = seckillSessionsWithSkus.getStartTime().getTime();
            Long endTime = seckillSessionsWithSkus.getEndTime().getTime();

            String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
            List<String> ids = seckillSessionsWithSkus.getSeckillSkuRelationEntities().stream().map((seckillSkuRelationEntity) -> {
                return seckillSkuRelationEntity.getSkuId().toString();
            }).collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(key, ids);
        });
    }

    /**
     * 2 缓存商品信息
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> seckillSessionWithSkuses) {

        seckillSessionWithSkuses.stream().forEach((seckillSessionsWithSkus) -> {
            //准备hash
            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            seckillSessionsWithSkus.getSeckillSkuRelationEntities().stream().forEach(seckillSkuVo -> {

                //缓存商品
                SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();

                //1 sku的基本数据
                R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                if (r.getCode() == 0) {
                    SkuInfoVo skuInfoVo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    seckillSkuRedisTo.setSkuInfoVo(skuInfoVo);
                }

                //2 sku的秒杀信息
                BeanUtils.copyProperties(seckillSkuVo, seckillSkuRedisTo);

                //3 sku的秒杀时间信息
                seckillSkuRedisTo.setStartTime(seckillSessionsWithSkus.getStartTime().getTime());
                seckillSkuRedisTo.setEndTime(seckillSessionsWithSkus.getEndTime().getTime());

                //4 秒杀随机码 : 防止恶意多刷 高并发
                String token = UUID.randomUUID().toString().replace("-", "");
                seckillSkuRedisTo.setRandomCode(token);

                /**
                 * 商品可以秒杀的数量(库存)作为信号量  信号量的作用 -- 限流
                 */
                RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                semaphore.trySetPermits(seckillSkuVo.getSeckillCount());

                String s = JSON.toJSONString(seckillSkuRedisTo);
                ops.put(seckillSkuVo.getSkuId().toString(), s);
            });
        });
    }
}
