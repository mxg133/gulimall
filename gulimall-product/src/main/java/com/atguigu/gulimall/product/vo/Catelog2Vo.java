package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 孟享广
 * @date 2021-01-06 12:33 下午
 * @description
 */

//无参构造器
@NoArgsConstructor
//全参构造器
@AllArgsConstructor
@Data
public class Catelog2Vo {

    private String catalog1Id; //一级父分类
    private List<Catalog3Vo> catalog3List; // 3级子分类
    private String id;
    private String name;
//    private Catalog3Vo catalog3Vo;


    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catalog3Vo {
        private String catalog2Id;//父分类 2级分类id
        private String id;
        private String name;
    }
}
