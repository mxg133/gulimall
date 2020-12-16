package com.atguigu.gulimall.order.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.PmsSpuImagesDao;
import com.atguigu.gulimall.order.entity.PmsSpuImagesEntity;
import com.atguigu.gulimall.order.service.PmsSpuImagesService;


@Service("pmsSpuImagesService")
public class PmsSpuImagesServiceImpl extends ServiceImpl<PmsSpuImagesDao, PmsSpuImagesEntity> implements PmsSpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsSpuImagesEntity> page = this.page(
                new Query<PmsSpuImagesEntity>().getPage(params),
                new QueryWrapper<PmsSpuImagesEntity>()
        );

        return new PageUtils(page);
    }

}