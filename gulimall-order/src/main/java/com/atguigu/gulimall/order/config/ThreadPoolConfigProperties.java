package com.atguigu.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 孟享广
 * @date 2021-01-27 4:12 下午
 * @description
 */
//跟配置文件绑定
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {

    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
