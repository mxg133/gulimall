package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-01-06 12:02 下午
 * @description
 */

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String  indexPage(Map<String, List<CategoryEntity>> map) {

        List<CategoryEntity> entities = categoryService.getLevel1Catrgorys();
        map.put("categorys", entities);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    //压力测试
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {

        return "hello";
    }
}
