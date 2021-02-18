package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-18 12:35 下午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-order")//这个远程服务
public interface OrderFeignService {

    /**
     * 给远程服务使用的 我的订单
     * 查询当前登录用户的所有订单详情数据（分页）
     * @RequestBody 远程传输必须用这个
     */
    @PostMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
