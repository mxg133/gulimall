package com.atguigu.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.coupon.entity.UmsMemberEntity;

import java.util.Map;

/**
 * »áÔ±
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:32
 */
public interface UmsMemberService extends IService<UmsMemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

