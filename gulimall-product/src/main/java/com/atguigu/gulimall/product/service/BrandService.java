package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.BrandEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 10:36:07
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //保证冗余字段的数据一致
    void updateDetail(BrandEntity brand);

    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

