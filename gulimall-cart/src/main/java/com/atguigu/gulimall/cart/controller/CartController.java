package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author 孟享广
 * @date 2021-02-03 2:22 下午
 * @description
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

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

    /**
     * 点击加入购物车后，跳转至成功页面
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, Map<String, CartItem> map) throws ExecutionException, InterruptedException {

        CartItem cartItem = cartService.addToCart(skuId, num);
        map.put("item", cartItem);

        return "success";
    }
}
