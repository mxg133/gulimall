package com.atguigu.gulimall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 引入sleuth链路追踪
 *  1 依赖 spring-cloud-starter-sleuth （common）
 *  2 yml logging:level:org.springframework.cloud.openfeign: debug;
 *     （这两个每个服务都应该有） org.springframework.cloud.sleuth: debug
 */
//注册到注册中心（发现中心）
@EnableDiscoveryClient
//排除跟数据源有关的配置
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
public class GulimallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallGatewayApplication.class, args);
    }

}
