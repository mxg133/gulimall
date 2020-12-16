package com.atguigu.gulimall.order.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.PmsSkuImagesDao;
import com.atguigu.gulimall.order.entity.PmsSkuImagesEntity;
import com.atguigu.gulimall.order.service.PmsSkuImagesService;


@Service("pmsSkuImagesService")
public class PmsSkuImagesServiceImpl extends ServiceImpl<PmsSkuImagesDao, PmsSkuImagesEntity> implements PmsSkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsSkuImagesEntity> page = this.page(
                new Query<PmsSkuImagesEntity>().getPage(params),
                new QueryWrapper<PmsSkuImagesEntity>()
        );

        return new PageUtils(page);
    }

}