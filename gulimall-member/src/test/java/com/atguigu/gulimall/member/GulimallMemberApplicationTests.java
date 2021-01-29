package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//@SpringBootTest
public class GulimallMemberApplicationTests {

    @Test
    public void contextLoads() {

        String s = DigestUtils.md5Hex("123456");
        System.out.println(s);//e10adc3949ba59abbe56e057f20f883e

        //颜值加密
        String s1 = Md5Crypt.md5Crypt("12345".getBytes(), "$1$qqqqqqqq");
        System.out.println(s1);

        //Spring家基于颜值加密为我们做好了
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //$2a$10$IxhnJo6iOmmSupKlzdn24OiZQrZj6aHEKK3qI0xHmL0CySGokWySu
        //$2a$10$7gUquDZcF/pOmQs9pDoYa.gTe1ALBwW1w5onjK.kBCC7PptkTdDLO
        String encode = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$IxhnJo6iOmmSupKlzdn24OiZQrZj6aHEKK3qI0xHmL0CySGokWySu");
        System.out.println(encode + " -> " +matches );
    }

}
