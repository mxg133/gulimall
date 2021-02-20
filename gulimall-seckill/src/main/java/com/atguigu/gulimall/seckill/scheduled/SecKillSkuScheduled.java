package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.Service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Autowired
    SeckillService seckillService;

    //TODO 幂等性处理
//    //异步任务 + 下
//    @Async
    //定时任务
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {

        //重复上架无需处理
        seckillService.uploadSeckillSkuLatest3Days();
        log.info("上架秒杀商品信息....");
    }

}
