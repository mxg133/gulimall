package com.atguigu.gulimall.cart.controller;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
     * 将商品添加到购物车
     * 防止用户恶意刷新，可以使用重定向的办法，本方法不跳转页面，只是执行完业务代码后，跳转到别的方法，让那个方法跳转页面
     * redirectAttributes.addFlashAttribute() 将数据保存在session里面可以在页面取出，但是只能取一次
     * redirectAttributes.addAttribute("skuId", skuId); 将数据放在URL后面
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        //业务代码
        cartService.addToCart(skuId, num);
        //将数据放在URL后面
        redirectAttributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    /**
     * 跳转至成功页面
     * 获取购物车中某个购物项
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Map<String, CartItem> map) {

        //重定向到成功页面，再次从Redis查询购物车数据即可
        CartItem cartItem = cartService.getCartItem(skuId);
        map.put("item", cartItem);

        return "success";
    }
}
