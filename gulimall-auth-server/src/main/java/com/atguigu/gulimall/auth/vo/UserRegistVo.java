package com.atguigu.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author 孟享广
 * @date 2021-01-29 1:20 下午
 * @description
 */
@Data
public class UserRegistVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6, max = 18, message = "用户名长度必须是6-18位")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6, max = 18, message = "密码长度必须是6-18位")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不对")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;
}
