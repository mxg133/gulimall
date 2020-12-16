package com.atguigu.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * skuÏúÊÛÊôÐÔ&Öµ
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 12:03:44
 */
@Data
@TableName("pms_sku_sale_attr_value")
public class PmsSkuSaleAttrValueEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * attr_id
	 */
	private Long attrId;
	/**
	 * ÏúÊÛÊôÐÔÃû
	 */
	private String attrName;
	/**
	 * ÏúÊÛÊôÐÔÖµ
	 */
	private String attrValue;
	/**
	 * Ë³Ðò
	 */
	private Integer attrSort;

}
