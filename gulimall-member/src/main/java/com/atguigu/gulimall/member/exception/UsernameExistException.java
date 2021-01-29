package com.atguigu.gulimall.member.exception;

/**
 * @author 孟享广
 * @date 2021-01-29 4:50 下午
 * @description
 */
public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("用户名已经存在");
    }
}
