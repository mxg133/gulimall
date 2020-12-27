package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 孟享广
 * @date 2020-12-27 12:19 下午
 * @description
 */
@Data
public class MergeVo {
    private Long purchaseId; //整单id
    private List<Long> items; //合并项集合
}
