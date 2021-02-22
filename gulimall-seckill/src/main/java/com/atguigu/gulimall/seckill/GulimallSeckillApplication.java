package com.atguigu.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 秒杀（高并发）系统关注的问题
 *
 * 01服务单一职责+独立部署
 *      秒杀服务即使自己扛不住压力，挂掉。不要影别人
 *
 * 02秒杀链接加密
 *      防止恶意攻击，模拟秒杀请求。1000次/s攻击。防止链接暴露，自己工作人员，提前秒杀商品
 *
 * 03库存预热+快速扣减
 * 04动静分离
 * ....
 * Sentinel高并发方法论&简介
 * 基本概念
 */
//开启feign客户度的远程调用功能
//扫描feign文件夹下的带有@FeignClient注解的接口
@EnableFeignClients(basePackages = "com.atguigu.gulimall.seckill.feign")
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
