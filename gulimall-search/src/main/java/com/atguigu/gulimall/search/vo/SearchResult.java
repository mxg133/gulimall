package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 孟享广
 * @date 2021-01-14 11:36 上午
 * @description
 */
@Data
public class SearchResult {

    //查询到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 以下是分页信息
     */
    private Integer pageNum;//当前页码
    private Long total;//总记录数
    private Integer totalPages;//总页码数
    private List<Integer> pageNavs;

    private List<BrandVo> brands;//当前查到的结果，所有涉及到的品牌
    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    private List<CatalogVo> catalogs;//当前查到的结果，所有涉及到的分类
    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    private List<AttrVo> attrs;//当前查询到的结果，所涉及到的所有属性
    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    //面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();
    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    private List<Long> attrIds = new ArrayList<>();
}
