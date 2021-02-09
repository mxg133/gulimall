package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author 孟享广
 * @date 2021-02-09 4:37 下午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-product")//这个远程服务
public interface ProductFeignService {

    /**
     * 根据skuId返回spu信息
     */
    @GetMapping("product/spuinfo/skuId/{id}")
    R getSpuInfoBuSkuId(@PathVariable("id") Long id);
}
