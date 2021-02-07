package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 孟享广
 * @date 2021-02-07 1:00 下午
 * @description
 */
@Controller
public class OrderWebController {

    @GetMapping("/toTrade")
    public String toTrade() {

        return "confirm";
    }
}
