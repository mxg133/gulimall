package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.Service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author 孟享广
 * @date 2021-02-20 10:55 上午
 * @description 秒杀商品到定时上架
 *  每天晚上3:00 ：上架最近三天需要秒杀的商品
 *  当天0点 - 23：59
 *  明天0点 - 23：59
 *  后天0点 - 23：59
 */
@Slf4j
@Service
public class SecKillSkuScheduled {

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    //TODO 幂等性处理
//    //异步任务 + 下
//    @Async
    //定时任务
    @Scheduled(cron = "0/10 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {

        log.info("上架秒杀商品信息....");
        //重复上架无需处理
        //加分布式锁 所有的业务执行完成，状态已经更新完成。释放，所以后期他人获取到就会拿到最新的状态(原子性)
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.MINUTES);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }

}
