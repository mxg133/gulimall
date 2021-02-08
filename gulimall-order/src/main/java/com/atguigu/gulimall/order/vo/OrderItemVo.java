package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 孟享广
 * @date 2021-02-07 2:32 下午
 * @description
 */
@Data
public class OrderItemVo {

    private Long skuId;
    private String title;
    private String image;
    //套餐信息
    private List<String> skuAttr;
    //涉及到计算 必须用BigDecimal
    private BigDecimal price;
    private Integer count;
    //总价
    private BigDecimal totalPrice;

    //TODO 是否有货 商品重量
//    private boolean hasStock;
    private BigDecimal weight;

}
