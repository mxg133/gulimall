package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-07 1:00 下午
 * @description
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 去结算
     * 给订单确认ye返回数据
     */
    @GetMapping("/toTrade")
    public String toTrade(Map<String, OrderConfirmVo> map) {

        OrderConfirmVo confirmVo = orderService.confirmOrder();
        map.put("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     * 展示订单确认页 数据
     */

}
