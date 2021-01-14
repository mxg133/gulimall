package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Service;

/**
 * @author 孟享广
 * @date 2021-01-14 10:04 上午
 * @description
 */

@Service
public class MallSearchServiceImpl implements MallSearchService {

    /**
     * 去es进行检索
     * @param param 检索的所有参数
     * @return 返回检索的结果
     */
    @Override
    public SearchResult search(SearchParam param) {

        //1 动态构建出查询需要的DSL语句
        return null;
    }
}
