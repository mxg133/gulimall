package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author 孟享广
 * @date 2021-01-05 12:54 下午
 * @description
 */
public interface ProductService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
