package com.atguigu.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author 孟享广
 * @date 2021-02-01 1:07 下午
 * @description
 */
//开启Spring Session功能 redis
@EnableRedisHttpSession
@Configuration
public class GulimallSessionConfig {

    /**
     * 自定义session作用域：整个网站
     * 使用一样的session配置，能保证全网站共享一样的session
     */
    @Bean
    public CookieSerializer cookieSerializer() {

        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();

        defaultCookieSerializer.setDomainName("gulimall.com");
        defaultCookieSerializer.setCookieName("GULISESSION");

        return defaultCookieSerializer;
    }

    /**
     * 序列化机制
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){

        return new GenericJackson2JsonRedisSerializer();
    }
}