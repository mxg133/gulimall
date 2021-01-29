package com.atguigu.gulimall.member.vo;

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
public class MemberRegistVo {

    private String userName;
    private String password;
    private String phone;
}
