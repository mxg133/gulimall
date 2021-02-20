package com.atguigu.gulimall.coupon.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author 孟享广
 * @date 2021-02-20 12:03 下午
 * @description
 */
public class CouponTimeForStringUtils {

    public static String startTimeString() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    public static String endTimeForString() {
        LocalDate now = LocalDate.now();
        LocalDate plus2 = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(plus2, max);
        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }
}
