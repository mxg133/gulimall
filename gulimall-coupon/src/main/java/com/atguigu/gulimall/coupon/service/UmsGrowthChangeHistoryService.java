package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.UmsGrowthChangeHistoryEntity;

import java.util.Map;

/**
 * ³É³¤Öµ±ä»¯ÀúÊ·¼ÇÂ¼
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:32
 */
public interface UmsGrowthChangeHistoryService extends IService<UmsGrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

