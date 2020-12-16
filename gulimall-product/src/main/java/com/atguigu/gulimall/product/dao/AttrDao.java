package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:06
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
