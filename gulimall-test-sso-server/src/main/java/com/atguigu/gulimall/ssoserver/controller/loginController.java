package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

/**
 * @author 孟享广
 * @date 2021-02-02 12:05 下午
 * @description
 */
@Controller
public class loginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

//client1.com:8081/employees
    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Map<String, String> map) {

        map.put("url", url);
        return "login";
    }

    @PostMapping("/dologin")
    public String dologin(@RequestParam("username")String username, @RequestParam("password")String password, @RequestParam("url")String url, HttpSession session) {

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            //登录成功
            System.out.println("登录成功");
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);
            session.setAttribute("loginUser", 1);
            return "redirect:" + url+"?token=" + uuid;
        }

        //登录失败，跳转到原来的页面
        System.out.println("登录失败");
        return "redirect:http://baidu.com";
    }
}
