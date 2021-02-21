package com.atguigu.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 孟享广
 * @date 2021-02-21 16:15 下午
 * @description
 */
//开启RabbitMQ消息队列 不监听消息可以不加
//@EnableRabbit
@Configuration
public class MyRabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }
}
