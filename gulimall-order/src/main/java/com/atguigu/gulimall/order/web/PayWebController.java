package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.utils.AlipayTemplate;
import com.atguigu.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 孟享广
 * @date 2021-02-17 3:02 下午
 * @description
 */
@Controller
public class PayWebController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    /**
     * 获取当前订单的支付信息 PayVo
     * 1 将支付页让浏览器显示
     * 2 支付成功以后，我们要跳到用户的订单列表页
     */
    @ResponseBody
    //text/html:告诉这里返回的是一个html的内容
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        //返回的是一个支付宝页面，应当将此页面直接交给浏览器
        PayVo payVo = orderService.getPayOrder(orderSn);
        String pay = alipayTemplate.pay(payVo);

        return pay;
    }
}
