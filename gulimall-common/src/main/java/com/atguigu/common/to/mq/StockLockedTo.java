package com.atguigu.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @author 孟享广
 * @date 2021-02-11 5:16 下午
 * @description
 */
@Data
public class StockLockedTo {

    //库存工作单id
    private Long id;

    //工作单详情的所有id
    private StockDetailTo detailTo;
}
