package com.atguigu.gulimall.order.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.PmsCommentReplayDao;
import com.atguigu.gulimall.order.entity.PmsCommentReplayEntity;
import com.atguigu.gulimall.order.service.PmsCommentReplayService;


@Service("pmsCommentReplayService")
public class PmsCommentReplayServiceImpl extends ServiceImpl<PmsCommentReplayDao, PmsCommentReplayEntity> implements PmsCommentReplayService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PmsCommentReplayEntity> page = this.page(
                new Query<PmsCommentReplayEntity>().getPage(params),
                new QueryWrapper<PmsCommentReplayEntity>()
        );

        return new PageUtils(page);
    }

}