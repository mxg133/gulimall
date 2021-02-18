package com.atguigu.gulimall.order.listener;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-18 2:26 下午
 * @description
 */
@RestController
public class OrderPayedIistener {

    @PostMapping("/payed/notify")
    public String handleAlipayed(HttpServletRequest request) {

        Map<String, String[]> map = request.getParameterMap();
        System.out.println("支付宝通知到位... 数据：" + map);
        //只要我们收到了，支付宝给我们的一步的通知，告诉我订单支付成功。返回success，支付宝就再也不通知
        return "success";
    }

}
