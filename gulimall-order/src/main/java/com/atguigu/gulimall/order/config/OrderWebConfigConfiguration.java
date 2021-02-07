package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 孟享广
 * @date 2021-02-03 3:57 下午
 * @description
 */
@Configuration
public class OrderWebConfigConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //拦截哪个拦截器的所有请求
        registry.addInterceptor(new LoginUserInterceptor()).addPathPatterns("/**");
    }
}
