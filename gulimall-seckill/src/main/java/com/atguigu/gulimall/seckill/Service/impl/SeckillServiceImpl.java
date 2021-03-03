package com.atguigu.gulimall.seckill.Service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.seckill.Service.SeckillService;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 孟享广
 * @date 2021-02-20 11:18 上午
 * @description
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    //活动信息
    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";
    //sku信息
    private final String SKUKILL_CACHE_PREFIX = "seckill:skus";//多了个:
    //高平发 信号量
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";//+随机码

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

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

    public List<SeckillSkuRedisTo> blockHandler(BlockException e) {

        log.info("getCurrentSeckillSkusResource资源被限流了.");
        return null;
    }

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * 被保护资源 try (Entry entry = SphU.entry("seckillSkus")) {
     * 被限流了就调用blockHandler = "blockHandler"方法
     * fallback = ""针对异常的处理
     */
    //定义被保护的资源
    @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

        //1 确定当前时间属于哪个秒杀场次
        //当前时间
        long now = new Date().getTime();
        try (Entry entry = SphU.entry("seckillSkus")) {
            Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
            for (String key : keys) {
                //seckill:sessions:1613757600000_1613761200000
                String replace = key.replace(SESSION_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                long start = Long.parseLong(s[0]);
                long end = Long.parseLong(s[1]);
                if (start <= now && now <= end) {
                    //说明在当前场次的时间区间内
                    List<String> row = redisTemplate.opsForList().range(key, -100, 100);
                    //已修改 BoundHashOperations<String, Object, Object> hashOps = r
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> list = hashOps.multiGet(row);
                    if (list != null) {
                        List<SeckillSkuRedisTo> collect = list.stream().map((item) -> {
                            SeckillSkuRedisTo to = JSON.parseObject((String) item, SeckillSkuRedisTo.class);
                            //                        to.setRandomCode(null);//当前秒杀开始就需要随机码
                            return to;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        } catch (BlockException e) {
            log.error("自定义被保护资源被限流-{}", e.getMessage());
        }
        return null;
    }

    /**
     * 给远程服务gulimall-product使用
     * 获取当前sku的秒杀预告信息
     */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {

        //1 找到所有需要参与秒杀的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d场->" + skuId;
            for (String key : keys) {
                //1场->1 正则 d表示匹配一个数字
                boolean b = Pattern.matches(regx, key);
                if (b) {
                    //匹配上了
                    String s = hashOps.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
                    //随机码需要处理 是不是在秒杀时间内
                    long now = new Date().getTime();
                    Long startTime = seckillSkuRedisTo.getStartTime();
                    Long endTime = seckillSkuRedisTo.getEndTime();
                    if (startTime <= now && now <= endTime) {
                        //在时间范围内部

                    } else {
                        //不在时间内部 随机码置空
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * 秒杀
     * http://seckill.gulimall.com/kill?killId=1_1&key=320c924165244276882adfaea84dac12&num=1
     * TODO 上架秒杀商品的时候，每一个数据都有过期时间
     * TODO 秒杀的后续流程，简化了收货地址等信息
     */
    @Override
    public String kill(String killId, String key, Integer num) {

        long s1 = System.currentTimeMillis();

        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

        //1 获取当前秒杀sku的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = hashOps.get(killId);
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
            //验证1 校验合法性
            long now = new Date().getTime();
            Long startTime = seckillSkuRedisTo.getStartTime();
            Long endTime = seckillSkuRedisTo.getEndTime();
            Long ttl = endTime - startTime;
            if (startTime <= now && now <= endTime) {
                //验证2 时间合法
                String randomCode = seckillSkuRedisTo.getRandomCode();
                String id = seckillSkuRedisTo.getPromotionSessionId() + "场->" + seckillSkuRedisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(id)) {
                    //合法数据 随机码匹配ok
                    //验证3 库存数量
                    BigDecimal limit = seckillSkuRedisTo.getSeckillLimit();
                    if (num <= Integer.parseInt(limit.toString())) {
                        //验证4 验证此人是否购买过 幂等性；如果秒杀成功，就去redis仅仅占位置
                        String newKey = memberResVo.getId() + "_" + id;
                        //自动过期 活动结束即结束
                        Boolean b = redisTemplate.opsForValue().setIfAbsent(newKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (b) {
                            //说明此人没买过
                            //触发信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            //不能被阻塞！最多等100ms
                            boolean b1 = semaphore.tryAcquire(num);
                            if (b1) {
                                //真正到了秒杀 快速下单
                                //创建订单号
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                seckillOrderTo.setOrderSn(timeId);
                                seckillOrderTo.setMemberId(memberResVo.getId());
                                seckillOrderTo.setNum(num);
                                seckillOrderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
                                seckillOrderTo.setSkuId(seckillSkuRedisTo.getSkuId());
                                seckillOrderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                long s2 = System.currentTimeMillis();
                                log.info("秒杀创建耗时：{}", (s2 - s1));
                                return timeId;
                            }
                            return null;
                        } else {
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * 1 缓存活动信息
     */
    private void saveSessionInfos(List<SeckillSessionsWithSkus> seckillSessionWithSkuses) {

        if (seckillSessionWithSkuses != null && seckillSessionWithSkuses.size() > 0) {
            seckillSessionWithSkuses.stream().forEach((seckillSessionsWithSkus) -> {
                Long startTime = seckillSessionsWithSkus.getStartTime().getTime();
                Long endTime = seckillSessionsWithSkus.getEndTime().getTime();

                String key = SESSION_CACHE_PREFIX + startTime + "_" + endTime;
                Boolean hasKey = redisTemplate.hasKey(key);
                if (!hasKey) {
                    List<String> ids = seckillSessionsWithSkus.getSeckillSkuRelationEntities().stream().map((seckillSkuRelationEntity) -> {
                        return seckillSkuRelationEntity.getPromotionSessionId() + "场->" + seckillSkuRelationEntity.getSkuId().toString();
                    }).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key, ids);
                }
            });
        }
    }

    /**
     * 2 缓存商品信息
     */
    private void saveSessionSkuInfos(List<SeckillSessionsWithSkus> seckillSessionWithSkuses) {
        if (seckillSessionWithSkuses != null && seckillSessionWithSkuses.size() > 0) {

            seckillSessionWithSkuses.stream().forEach((seckillSessionsWithSkus) -> {
                //准备hash
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                seckillSessionsWithSkus.getSeckillSkuRelationEntities().stream().forEach(seckillSkuVo -> {

                    //生成随机码
                    String token = UUID.randomUUID().toString().replace("-", "");

                    Boolean hasKey1 = ops.hasKey(seckillSkuVo.getPromotionSessionId().toString() + "场->" + seckillSkuVo.getSkuId().toString());
                    if (!hasKey1) {
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
                        seckillSkuRedisTo.setRandomCode(token);

                        String s = JSON.toJSONString(seckillSkuRedisTo);
                        ops.put(seckillSkuVo.getPromotionSessionId().toString() + "场->" + seckillSkuVo.getSkuId().toString(), s);

                        /**
                         * 5 商品可以秒杀的数量(库存)作为信号量  信号量的作用 -- 限流
                         * 如果当前这个场次的商品的库存信息已经上架就不需要上架
                         */
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
//                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                        semaphore.trySetPermits(Integer.parseInt(seckillSkuVo.getSeckillCount().toString()));
                    }
                });
            });
        }
    }
}
