package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.Service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-20 5:36 下午
 * @description
 */
@Slf4j
@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        log.info("/currentSeckillSkus正在执行..");

        List<SeckillSkuRedisTo> seckillSkuRedisTos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(seckillSkuRedisTos);
    }

    /**
     * 给远程服务gulimall-product使用
     * 获取当前sku的秒杀预告信息
     */
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId) {

//        try {
//            Thread.sleep(300);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        SeckillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    /**
     * 秒杀
     * http://seckill.gulimall.com/kill?killId=1_1&key=320c924165244276882adfaea84dac12&num=1
     */
    @GetMapping("/kill")
    public String getKill(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") Integer num, Map<String, String> map){

        //后端再次验证是否登录
        String orderSn = seckillService.kill(killId, key, num);
        map.put("orderSn", orderSn);
        return "success";
    }
}
