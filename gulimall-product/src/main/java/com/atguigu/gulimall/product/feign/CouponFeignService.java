package com.atguigu.gulimall.product.feign;

import com.atguigu.common.to.SkuReductionTo;
import com.atguigu.common.to.SpuBoundsTo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 孟享广
 * @date 2020-12-26 3:19 下午
 * @description
 */

//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-coupon")//这个远程服务
public interface CouponFeignService {
    /**
     * 1、CouponFeignService.saveSpuBounds(spuBoundTo);
     *      1）、@RequestBody将这个对象转为json。
     *      2）、找到gulimall-coupon服务，给/coupon/spubounds/save发送请求。
     *          将上一步转的json放在请求体位置，发送请求；
     *      3）、对方服务收到请求。请求体里有json数据。
     *          (@RequestBody SpuBoundsEntity spuBounds)；将请求体的json转为SpuBoundsEntity；
     * 只要json数据模型是兼容的。双方服务无需使用同一个to
     */
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @RequestMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}