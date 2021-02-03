package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 孟享广
 * @date 2021-02-03 2:22 下午
 * @description
 */
@Controller
public class CartController {

    /**
     * 浏览器有一个cookie：user-key:标识用户身份  一个月过期
     * 假如是第一次登录，都会给一个临时身份
     * 浏览器保存以后，每次访问都会带上这个cookie
     */
    @GetMapping("/cart.html")
    public String cartListPage() {

        //1 快速得到用户信息，id user-key
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        System.out.println(userInfoTo);


        return "cartList";
    }
}
