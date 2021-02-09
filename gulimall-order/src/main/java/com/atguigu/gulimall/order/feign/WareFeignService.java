package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author 孟享广
 * @date 2021-02-08 1:39 下午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-ware")//这个远程服务
public interface WareFeignService {

    //远程查询商品的库存信息 查询sku是否有库存
    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    /**
     * 根据用户的收货地址计算运费
     */
    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    /**
     * 为某个订单锁定库存
     */
    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
