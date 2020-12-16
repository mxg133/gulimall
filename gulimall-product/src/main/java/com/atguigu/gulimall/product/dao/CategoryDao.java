package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:07
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
