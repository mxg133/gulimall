package com.atguigu.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ÊôÐÔ·Ö×é
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:31
 */
@Data
@TableName("pms_attr_group")
public class PmsAttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ·Ö×éid
	 */
	@TableId
	private Long attrGroupId;
	/**
	 * ×éÃû
	 */
	private String attrGroupName;
	/**
	 * ÅÅÐò
	 */
	private Integer sort;
	/**
	 * ÃèÊö
	 */
	private String descript;
	/**
	 * ×éÍ¼±ê
	 */
	private String icon;
	/**
	 * ËùÊô·ÖÀàid
	 */
	private Long catelogId;

}
