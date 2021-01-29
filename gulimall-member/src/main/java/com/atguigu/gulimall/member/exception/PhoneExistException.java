package com.atguigu.gulimall.member.exception;

/**
 * @author 孟享广
 * @date 2021-01-29 4:51 下午
 * @description
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号已经存在");
    }
}
