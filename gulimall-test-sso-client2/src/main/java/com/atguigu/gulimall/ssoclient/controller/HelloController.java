package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-02 11:20 上午
 * @description
 */
@Controller
public class HelloController {

    //登录服务器的地址
    @Value("${sso.server.url}")
    String ssoServerUrl;

    /**
     * 无需登录，即可访问
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {

        return "hello";
    }

    /**
     * 需要登录，才能访问
     * 需要感知 ssoserver 登录成功跳回来的
     * required 非必须带有这个参数 登录成功才有
     */
    @GetMapping("/boss")
    public String employees(Map<String, List<String>> map, @RequestParam(value = "token", required = false) String token, HttpSession session) {

        //判断是否登录
        if (!StringUtils.isEmpty(token)) {
            //TODO 去ssosercer获取当前token真正对应的用户信息 发请求的代码
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            //session真正的用户
            String body = forEntity.getBody();
            session.setAttribute("loginUser", body);
        }

        //先到这里！
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //没登录，去登录服务器
            System.out.println("没登录，去登录服务器");
            //跳转以后 使用查询参数表示是我这里请求的
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        }else {
            //登录了
            System.out.println("登录了");
            List<String> emps = new ArrayList<>();
            emps.add("高佳好2");
            emps.add("刘艺璇2");
            map.put("emps", emps);
            return "list";
        }
    }
}
