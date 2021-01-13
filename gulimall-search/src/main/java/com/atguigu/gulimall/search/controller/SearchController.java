package com.atguigu.gulimall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 孟享广
 * @date 2021-01-13 5:24 下午
 * @description
 */

@Controller
public class SearchController {

    @GetMapping("list.html")
    public String listPage() {
        return "list";
    }
}
