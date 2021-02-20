package com.atguigu.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 孟享广
 * @date 2021-02-19 2:37 下午
 * @description
 *  @EnableScheduling 开启定时任务功能
 *  @EnableAsync 开启异步任务功能
 */
//开启异步任务功能
@EnableAsync
//开启定时任务功能
@EnableScheduling
@Configuration
public class MyScheduledConfig {
}
