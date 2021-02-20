package com.atguigu.gulimall.coupon;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GulimallCouponApplicationTests {

    @Test
    public void contextLoads() {

        LocalDate now = LocalDate.now();
        LocalDate plus1 = now.plusDays(1);
        LocalDate plus2 = now.plusDays(2);
        System.out.println(now);
        System.out.println(plus1);
        System.out.println(plus2);

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;
        System.out.println(min);
        System.out.println(max);

        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(plus2, max);
        System.out.println(start);
        System.out.println(end);
    }

    @Test
    public void test() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format);

        LocalDate plus2 = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(plus2, max);
        String format1 = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format1);
    }

}
