package com.atguigu.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 孟享广
 * @date 2021-01-14 9:49 上午
 * @description
 *      封装页面所有可能传递过来的查询条件
 */

@Data
public class SearchParam {

    private String keyword;//页面传递过来的检索参数 相当于全文匹配关键字
    private Long catalog3Id;//三级分类id

    /**
     * 排序条件
     *  sort=saleCount_asc/desc 倒序
     *  sort=skuPrice_asc/desc 根据价格
     *  sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * hasStock(是否有货) skuPrice区间 brandId catalog3Id attrs
     * hasStock 0/1
     * skuPrice=1_500 500_ _500
     * brandId = 1
     * attrs1_5寸_6寸
     * // 0 无库存 1有库存
     */
    private Integer hasStock;

    /**
     * 价格区查询
     */
    private String skuPrice;

    /**
     * 多个品牌id
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生所有的查询条件
     */
    private String _queryString;
}
