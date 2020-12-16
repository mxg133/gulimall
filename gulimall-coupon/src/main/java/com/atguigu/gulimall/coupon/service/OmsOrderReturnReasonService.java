package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.OmsOrderReturnReasonEntity;

import java.util.Map;

/**
 * ÍË»õÔ­Òò
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:31
 */
public interface OmsOrderReturnReasonService extends IService<OmsOrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

