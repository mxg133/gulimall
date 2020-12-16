package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:47:42
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
