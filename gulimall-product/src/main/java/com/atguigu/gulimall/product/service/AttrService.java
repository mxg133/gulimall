package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 商品属性
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:06
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

