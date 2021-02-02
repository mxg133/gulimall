package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 孟享广
 * @date 2021-02-02 12:05 下午
 * @description
 */
@Controller
public class loginController {

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String redirect_url) {

        return "login";
    }

    @PostMapping("/dologin")
    public String dologin() {

        //登录成功，跳转到原来的页面
        return "";
    }
}
