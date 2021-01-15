package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-01-13 5:24 下午
 * @description
 */

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("list.html")
    public String listPage(SearchParam param, Model model) {
        //1 根据传递来的页面的查询参数，去ES中解锁商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result",result);
        return "list";
    }
}
