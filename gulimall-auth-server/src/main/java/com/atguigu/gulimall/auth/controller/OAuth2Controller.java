package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.gulimall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-01-31 2:03 下午
 * @description 处理社交登录请求
 */
@Controller
public class OAuth2Controller {

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code) throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("client_id", "1133714539");
        map.put("client_secret", "f22eb330342e7f8797a7dbe173bd9424");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        //1 根据 code 换取 access_token 能获取则成功
        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post", null, null, map);

        //处理返回的 response 这个 json
        if (response.getStatusLine().getStatusCode() == 200) {
            //成功获取了access_token JSON逆转为对象
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //得知道是哪个社交用户登录的
            //1） 第一次用 微博 进行 社交登录=>注册，进行一对一绑定注册到远程服务的数据库
            //2） 多次用 微博 进行 社交登录=>登录
            //由远程服务来做判断


            //2 登录成功 -> 跳转首页
            return "redirect:http://gulimall.com";
        }else {
            //没有获取了access_token 登录失败 返回到登录页
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}