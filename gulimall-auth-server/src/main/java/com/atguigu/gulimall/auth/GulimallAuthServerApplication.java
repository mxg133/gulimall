package com.atguigu.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//开启feign客户度的远程调用功能gulimall-auth-server
//扫描feign文件夹下的带有@FeignClient注解的接口
@EnableFeignClients(basePackages = "com.atguigu.gulimall.auth.feign")
@EnableDiscoveryClient
@SpringBootApplication
/**
 * Spring Session 核心原理
 * @EnableRedisHttpSession 导入RedisHttpSessionConfiguration配置
 * 装饰者模式
 * 自动延期 redis中的数据是有过期时间的
 */
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
