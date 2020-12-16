package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * sku信息
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:06
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

