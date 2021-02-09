package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.concurrent.ExecutionException;

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
    public String toTrade(Map<String, OrderConfirmVo> map) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.confirmOrder();
        map.put("orderConfirmData", confirmVo);
        //展示订单确认页 数据
        return "confirm";
    }

    /**
     * 提交订单 去支付
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo) {

        //

        //下单成功来到支付选择页

        //下单失败，回到订单确认页重新提交订单信息

        System.out.println("订单提交的数据"+vo);
        return "";
    }

}
