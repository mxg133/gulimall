package com.atguigu.gulimall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//开启feign客户度的远程调用功能gulimall-cart
//扫描feign文件夹下的带有@FeignClient注解的接口
@EnableFeignClients(basePackages = "com.atguigu.gulimall.cart.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCartApplication.class, args);
    }

}
