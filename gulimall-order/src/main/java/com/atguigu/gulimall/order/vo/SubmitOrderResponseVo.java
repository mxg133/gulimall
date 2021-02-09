package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author 孟享广
 * @date 2021-02-09 1:47 下午
 * @description
 */
@Data
public class SubmitOrderResponseVo {

    //下单成功返回这个实体
    private OrderEntity order;
    //下单错误给一个状态码 0:成功
    private Integer code;
}
