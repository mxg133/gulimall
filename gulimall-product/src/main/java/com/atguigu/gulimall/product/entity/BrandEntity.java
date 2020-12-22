package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:07
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 * groups 在不同条件下出发的，
	 *     为注解标注了适用于哪一种情况才去校验
	 */
	@NotNull(message = "修改的时候，必须指定品牌id!", groups = {UpdateGroup.class}) //修改的时候 不能为空
	@Null(message = "新增的时候，不能指定品牌id!", groups = {AddGroup.class}) //新增的时候 必须为空
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 * message 替换了默认的错误提示
	 */
	@NotBlank(message = "name品牌名需要提交！", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(groups = {AddGroup.class})
	@URL(message = "logo需要是一个合法的url地址！", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(vals={0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 * 自定义规则 -- 正则
	 */
	@NotNull(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "firstLetter检索首字母需要是一个字母！", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "sort排序需要大于等于0！", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
