package com.atguigu.gulimall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 孟享广
 * @date 2021-02-17 7:44 下午
 * @description
 */
@Controller
public class MemberWebController {

    @GetMapping("/memberOrder.html")
    public String memberOrderPage() {

        return "orderList";
    }
}
