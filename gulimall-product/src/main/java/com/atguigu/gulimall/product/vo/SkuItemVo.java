package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 孟享广
 * @date 2021-01-26 12:01 下午
 * @description
 */

@Data
public class SkuItemVo {

    //1 sku基本信息获取 pms_sku_info
    SkuInfoEntity info;

    //2 sku图片信息 pms_sku_images
    List<SkuImagesEntity> images;

    //3 spu的销售属性组合
    List<SkuItemSaleAttrVo> saleAttr;
    @Data
    public static class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    //4 spu的介绍
    SpuInfoDescEntity desc;

    //5 spu的规格参数信息
    private List<SpuItemAttrGroupVo> groupAttrs;
    @Data
    public static class SpuItemAttrGroupVo {
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }
    @Data
    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValues;
    }
}
