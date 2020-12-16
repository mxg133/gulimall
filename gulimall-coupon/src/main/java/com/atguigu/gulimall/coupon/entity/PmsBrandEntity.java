package com.atguigu.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Æ·ÅÆ
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:31
 */
@Data
@TableName("pms_brand")
public class PmsBrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Æ·ÅÆid
	 */
	@TableId
	private Long brandId;
	/**
	 * Æ·ÅÆÃû
	 */
	private String name;
	/**
	 * Æ·ÅÆlogoµØÖ·
	 */
	private String logo;
	/**
	 * ½éÉÜ
	 */
	private String descript;
	/**
	 * ÏÔÊ¾×´Ì¬[0-²»ÏÔÊ¾£»1-ÏÔÊ¾]
	 */
	private Integer showStatus;
	/**
	 * ¼ìË÷Ê××ÖÄ¸
	 */
	private String firstLetter;
	/**
	 * ÅÅÐò
	 */
	private Integer sort;

}
