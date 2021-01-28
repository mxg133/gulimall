package com.atguigu.gulimall.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 孟享广
 * @date 2021-01-28 11:34 上午
 * @description
 */
@Controller
public class LoginRegController {

    /**
     * 下面两个空方法仅仅是发送一个请求【直接】跳转一个页面
     * 这样不太好 不要写空方法 去GulimallWebConfig.class
     * 使用 SpringMVC ViewController 将请求和页面映射过来
     */
//    @GetMapping("/login.html")
//    public String loginPage() {
//
//        return "login";
//    }
//
//    @GetMapping("/reg.html")
//    public String regPage() {
//
//        return "reg";
//    }
}
