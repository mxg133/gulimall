package com.atguigu.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author 孟享广
 * @date 2021-02-19 2:36 下午
 * @description
 * 定时任务
 *  @EnableScheduling 开启定时任务
 *  @Scheduled 开启一个定时任务
 *  异步任务：
 *       1、@EnableAsync 开启异步任务
 *       2、@Async 给希望异步执行的方法上批注
 *       3、自动配置类 TaskExecutionAutoConfiguration 属性绑定了TaskExecutionProperties
 */
@Slf4j
@Component
public class HelloScheduled {

    /**
     * 1、Spring由6位组成  不允许第七位年
     * 2、在周几位置 1234567 代表 周一到周日
     * 3、定时任务不应该阻塞 悲哀的是:默认是阻塞的
     *      1）、可以让业务运行已异步的方式 自己提交到线程池
     *              CompletableFuture.runAsync(()->{
     *                  xxxService.hello();
     *              })
     *      2）、支持定时任务线程池 设置TaskSchedulingProperties
     *             spring.task.scheduling.pool.size=5
     *      3）、让定时任务异步执行
     *          异步任务：
     *
     *          解决异步+定时任务来完成定时任务
     * 秒 分 时 日 月
     * cron="7-9,23 * * * * ?"
     * 常用cron表达式
     *      0 0 10,14,16 * * ?           每天上午10点，下午2点，4点
     *      0 0/30 9-17 * * ?            朝九晚五工作时间内每半小时
     *      0 0 12 ? * WED               表示每个星期三中午12点
     *      "0 0 12 * * ?"               每天中午12点触发
     *      "0 15 10 ? * *"              每天上午10:15触发
     *      "0 15 10 * * ?"              每天上午10:15触发
     *      "0 15 10 * * ? *"            每天上午10:15触发
     *      "0 15 10 * * ? 2005"         2005年的每天上午10:15触发
     *      "0 * 14 * * ?"               在每天下午2点到下午2:59期间的每1分钟触发
     *      "0 0/5 14 * * ?"             在每天下午2点到下午2:55期间的每5分钟触发
     *      "0 0/5 14,18 * * ?"          在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
     *      "0 0-5 14 * * ?"             在每天下午2点到下午2:05期间的每1分钟触发
     *      "0 10,44 14 ? 3 WED"         每年三月的星期三的下午2:10和2:44触发
     *      "0 15 10 ? * MON-FRI"        周一至周五的上午10:15触发
     *      "0 15 10 15 * ?"             每月15日上午10:15触发
     *      "0 15 10 L * ?"              每月最后一日的上午10:15触发
     *      "0 15 10 ? * 6L"             每月的最后一个星期五上午10:15触发
     *      "0 15 10 ? * 6L 2002-2005"   2002年至2005年的每月的最后一个星期五上午10:15触发
     *      "0 15 10 ? * 6#3"            每月的第三个星期五上午10:15触发
     */
    //异步任务 + 下
//    @Async
    //定时任务
//    @Scheduled(cron = "* * * * * *")
    public void hello() throws InterruptedException {
        log.info("hello .. .");
        Thread.sleep(3000);
    }
}
