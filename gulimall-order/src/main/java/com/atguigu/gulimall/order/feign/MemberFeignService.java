package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author 孟享广
 * @date 2021-02-07 3:00 下午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-member")//这个远程服务
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/getAddress")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);

}
