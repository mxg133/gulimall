package com.atguigu.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 孟享广
 * @date 2021-02-07 5:03 下午
 * @description 解决Feign远程调用请求头丢失
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {

        return new RequestInterceptor() {

            @Override
            public void apply(RequestTemplate template) {

                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null) {
                        //同步请求头数据 主要同步Cookie
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步的老请求Cookie
                        template.header("Cookie", cookie);
                    }
                }
            }
        };
    }
}
