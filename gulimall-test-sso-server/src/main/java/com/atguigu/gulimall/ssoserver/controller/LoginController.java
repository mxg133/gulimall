package com.atguigu.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * @author 孟享广
 * @date 2021-02-02 12:05 下午
 * @description
 * 1 给登录服务器留下登录痕迹
 * 2 登录服务器要江头看信息重定向的时候带到URL地址上
 * 3 其他系统要处理幼儿的地址上，关键偷看只要有，将陶侃对应的用户保存到自己的30分钟
 * 4 自己系统将用户保存在自己的绘画中
 */
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping("userInfo")
    public String userInfo(@RequestParam("token") String token) {

        String s = stringRedisTemplate.opsForValue().get(token);
        return s;
    }

//client1.com:8081/employees
//127.0.0.1 client1.com
//127.0.0.1 client2.com
//127.0.0.1 ssoserver.com

    /**
     * 远端的注册请求地址，跳转至登录页面
     */
    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String redirect_url,
                            Map<String, String> map,
                            @CookieValue(value = "sso_token", required = false) String sso_token) {

        //判断浏览器是否有Cookie(sso_token)，即之前是否有人登录过
        if (!StringUtils.isEmpty(sso_token)) {
            //之前有人登录过，给浏览器留下了痕迹，把这个sso_token返回去
            return "redirect:" + redirect_url+"?token=" + sso_token;
        }
        //正常执行登录
        map.put("url", redirect_url);
        return "login";
    }

    /**
     * 点击 提交
     */
    @PostMapping("/dologin")
    public String dologin(@RequestParam("username")String username,
                          @RequestParam("password")String password,
                          @RequestParam("url")String url,
                          HttpServletResponse response) {

        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            //登录成功
            System.out.println("登录成功");
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);
            //命令浏览器保存一个cookie  意思是有人登录成功，就留一个cookie
            //cookie名就是sso_token 值就只用户的唯一id
            Cookie cookie = new Cookie("sso_token", uuid);
            response.addCookie(cookie);
            //重定向前，要保存上面的cookie 在sso_server域名下，以后浏览器访问这个域名，都要带上这个域名下的所有cookie
            return "redirect:" + url+"?token=" + uuid;
        }

        //登录失败，跳转到原来的页面
        System.out.println("登录失败");
        return "redirect:http://3.cn";
    }
}
