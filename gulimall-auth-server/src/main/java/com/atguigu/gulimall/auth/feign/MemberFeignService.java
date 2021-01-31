package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.vo.MemberRegistVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 孟享广
 * @date 2021-01-30 9:42 上午
 * @description
 */
//告诉spring cloud 这个接口是一个远程客户端 调用远程服务
@FeignClient("gulimall-member")//这个远程服务
@Component
public interface MemberFeignService {

    //注册
    @PostMapping("/member/member/regist")
    R regist(@RequestBody MemberRegistVo vo);

    //登录
    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    //社交
    //社交登录
    @PostMapping("/member/member/oauth/login")
    R oauthLogin(@RequestBody SocialUser vo) throws Exception;
}
