package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @author 孟享广
 * @date 2020-12-27 4:40 下午
 * @description
 */

@Data
public class PurchaseItemDoneVo {

    private Long itemId;
    private Integer status;
    private String reason;
}
