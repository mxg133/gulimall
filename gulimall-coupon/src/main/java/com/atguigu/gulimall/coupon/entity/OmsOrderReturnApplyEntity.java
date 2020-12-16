package com.atguigu.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ¶©µ¥ÍË»õÉêÇë
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 11:38:31
 */
@Data
@TableName("oms_order_return_apply")
public class OmsOrderReturnApplyEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * order_id
	 */
	private Long orderId;
	/**
	 * ÍË»õÉÌÆ·id
	 */
	private Long skuId;
	/**
	 * ¶©µ¥±àºÅ
	 */
	private String orderSn;
	/**
	 * ÉêÇëÊ±¼ä
	 */
	private Date createTime;
	/**
	 * »áÔ±ÓÃ»§Ãû
	 */
	private String memberUsername;
	/**
	 * ÍË¿î½ð¶î
	 */
	private BigDecimal returnAmount;
	/**
	 * ÍË»õÈËÐÕÃû
	 */
	private String returnName;
	/**
	 * ÍË»õÈËµç»°
	 */
	private String returnPhone;
	/**
	 * ÉêÇë×´Ì¬[0->´ý´¦Àí£»1->ÍË»õÖÐ£»2->ÒÑÍê³É£»3->ÒÑ¾Ü¾ø]
	 */
	private Integer status;
	/**
	 * ´¦ÀíÊ±¼ä
	 */
	private Date handleTime;
	/**
	 * ÉÌÆ·Í¼Æ¬
	 */
	private String skuImg;
	/**
	 * ÉÌÆ·Ãû³Æ
	 */
	private String skuName;
	/**
	 * ÉÌÆ·Æ·ÅÆ
	 */
	private String skuBrand;
	/**
	 * ÉÌÆ·ÏúÊÛÊôÐÔ(JSON)
	 */
	private String skuAttrsVals;
	/**
	 * ÍË»õÊýÁ¿
	 */
	private Integer skuCount;
	/**
	 * ÉÌÆ·µ¥¼Û
	 */
	private BigDecimal skuPrice;
	/**
	 * ÉÌÆ·Êµ¼ÊÖ§¸¶µ¥¼Û
	 */
	private BigDecimal skuRealPrice;
	/**
	 * Ô­Òò
	 */
	private String reason;
	/**
	 * ÃèÊö
	 */
	private String descriptionêö;
	/**
	 * Æ¾Ö¤Í¼Æ¬£¬ÒÔ¶ººÅ¸ô¿ª
	 */
	private String descPics;
	/**
	 * ´¦Àí±¸×¢
	 */
	private String handleNote;
	/**
	 * ´¦ÀíÈËÔ±
	 */
	private String handleMan;
	/**
	 * ÊÕ»õÈË
	 */
	private String receiveMan;
	/**
	 * ÊÕ»õÊ±¼ä
	 */
	private Date receiveTime;
	/**
	 * ÊÕ»õ±¸×¢
	 */
	private String receiveNote;
	/**
	 * ÊÕ»õµç»°
	 */
	private String receivePhone;
	/**
	 * ¹«Ë¾ÊÕ»õµØÖ·
	 */
	private String companyAddress;

}
