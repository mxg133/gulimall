package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 12:15:27
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
