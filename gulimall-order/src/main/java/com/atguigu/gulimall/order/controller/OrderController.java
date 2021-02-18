package com.atguigu.gulimall.order.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;

/**
 * 订单
 *
 * @author mxg
 * @email mxg@gmail.com
 * @date 2020-12-16 12:03:44
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 按照订单号查询订单
     */
    @GetMapping("/status/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn) {

        OrderEntity entity = orderService.getOrderByOrderSn(orderSn);
        return R.ok().setData(entity);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 给远程服务使用的
     * 查询当前登录用户的所有订单详情数据（分页）
     * @RequestBody 远程传输必须用这个
     */
    @PostMapping("/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params) {

        PageUtils page = orderService.queryPageWithItem(params);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody OrderEntity order) {
        orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody OrderEntity order) {
        orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
