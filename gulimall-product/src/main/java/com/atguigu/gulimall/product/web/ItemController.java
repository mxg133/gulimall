package com.atguigu.gulimall.product.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 孟享广
 * @date 2021-01-26 11:43 上午
 * @description
 */

@Controller
public class ItemController {

    /**
     * 展示当前sku的详情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId) {
        System.out.println("准备查询 " + skuId);
        return "item";
    }
}
