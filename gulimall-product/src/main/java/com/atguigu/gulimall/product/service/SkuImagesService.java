package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * sku图片
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:06
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

