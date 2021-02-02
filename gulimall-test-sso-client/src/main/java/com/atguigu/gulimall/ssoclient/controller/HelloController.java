package com.atguigu.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
     */
    @GetMapping("/employees")
    public String employees(Map<String, List<String>> map, HttpSession session) {

        //判断是否登录
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //没登录，去登录服务器
            //跳转以后 使用查询参数表示是我这里请求的
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client1.com:8081/employees";
        }else {
            //登录了
            List<String> emps = new ArrayList<>();
            emps.add("高佳好");
            emps.add("刘艺璇");
            map.put("emps", emps);
            return "list";
        }
    }
}
