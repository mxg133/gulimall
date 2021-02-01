package com.atguigu.gulimall.search;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

//开启Spring Session功能 redis
@EnableRedisHttpSession
//开启feign客户度的远程调用功能
//扫描feign文件夹下的带有@FeignClient注解的接口
@EnableFeignClients(basePackages = "com.atguigu.gulimall.search.feign")
@EnableDiscoveryClient
//排除跟数据源有关的配置
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class})
public class GulimallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApplication.class, args);
    }

}
