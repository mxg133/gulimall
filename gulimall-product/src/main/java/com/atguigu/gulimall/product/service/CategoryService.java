package com.atguigu.gulimall.product.service;

import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:07
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();


    void removeMenuByIds(List<Long> asList);

    /**
     * 找到catelogId完整路径
     * 父/子/孙
     */
    Long[] findCatelogPath(Long catelogId);

    //级联更新 所有数据
    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Catrgorys();

    Map<String, List<Catelog2Vo>> getCatalogJson();
}

