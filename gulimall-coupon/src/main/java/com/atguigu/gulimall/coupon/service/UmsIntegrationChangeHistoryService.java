package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.UmsIntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * »ý·Ö±ä»¯ÀúÊ·¼ÇÂ¼
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:32
 */
public interface UmsIntegrationChangeHistoryService extends IService<UmsIntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

