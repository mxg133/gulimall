package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServiceConstant;
import com.atguigu.common.constant.CartServiceConstant;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author 孟享广
 * @date 2021-02-03 3:01 下午
 * @description 在执行目标方法之前，判断用户的登录状态，并封装传递给controller的目标请求
 */
//@Component
public class CartInterceptor implements HandlerInterceptor {

    //ThreadLocal 同一线程上信息共享
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberResVo memberResVo = (MemberResVo) session.getAttribute(AuthServiceConstant.LOGIN_USER);
        if (memberResVo != null) {
            //说明用户登录了
            userInfoTo.setUserId(memberResVo.getId());
        }

        //只要有user-key 就赶紧取出value
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            //有cookie 可能是临时用户，但是此方法针对登录用户
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartServiceConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    //执行到这，说明是临时用户
                    userInfoTo.setTempUser(true);
                }
            }
        }

        //如果没有登录 就准备临时set一个cookie，首先设置To的userKey
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        //目标方法执行前，放入 threadLocal
        threadLocal.set(userInfoTo);

        //只要来到目标方法都放行 无条件放行
        return true;
    }

    /**
     * 业务执行之后，让浏览器保存cookie
     * 分配临时用户，让浏览器保存
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();
        //如果没有临时用户，一定要保存一个临时用户
        if (!userInfoTo.isTempUser()) {
            //不是临时用户
            Cookie cookie = new Cookie(CartServiceConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            //设置cookie作用域
            cookie.setDomain("gulimall.com");
            //cookie的过期时间
            cookie.setMaxAge(CartServiceConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
