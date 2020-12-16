package com.atguigu.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.coupon.entity.PmsProductAttrValueEntity;
import com.atguigu.gulimall.coupon.service.PmsProductAttrValueService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * spuÊôÐÔÖµ
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:31
 */
@RestController
@RequestMapping("coupon/pmsproductattrvalue")
public class PmsProductAttrValueController {
    @Autowired
    private PmsProductAttrValueService pmsProductAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = pmsProductAttrValueService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PmsProductAttrValueEntity pmsProductAttrValue = pmsProductAttrValueService.getById(id);

        return R.ok().put("pmsProductAttrValue", pmsProductAttrValue);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PmsProductAttrValueEntity pmsProductAttrValue){
		pmsProductAttrValueService.save(pmsProductAttrValue);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PmsProductAttrValueEntity pmsProductAttrValue){
		pmsProductAttrValueService.updateById(pmsProductAttrValue);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		pmsProductAttrValueService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
