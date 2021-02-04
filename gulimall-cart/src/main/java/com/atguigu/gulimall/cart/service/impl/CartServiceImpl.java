package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 孟享广
 * @date 2021-02-03 1:36 下午
 * @description
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    //购物车前缀
    private final String CART_PREFIX = "gulimall:cart:";


    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        //获取到我们要操作的购物车
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        //先判断是新增商品，还是仅仅是增加数量
        String result = (String) cartOps.get(skuId.toString());
        if (StringUtils.isEmpty(result)) {
            //说明购物车没有此商品，新增商品类型
            //2 要存入Redis的大对象 要返回的大对象
            CartItem cartItem = new CartItem();
            //1 远程查询当前要操作的商品信息 获得真正的sku商品信息 并封装
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setSkuId(skuId);
                cartItem.setPrice(skuInfo.getPrice());
            }, executor);

            //3 远程查询sku属性的组合信息
            CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(attrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();

            //操作Redis 每次执行本方法，都put一下
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);

            return cartItem;
        } else {
            //说明是仅仅增加数量
            //2 要存入Redis的大对象 要返回的大对象
            CartItem cartItem = JSON.parseObject(result, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);

            //操作Redis 每次执行本方法，都put一下
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(), s);
            return cartItem;
        }
    }

    /**
     * 获取到我们要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey;
        if (userInfoTo.getUserId() != null) {
            //说明用户登录了，Redis存入带UserId gulimall:cart:11
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            //说明是临时用户，Redis存入带uuid gulimall:cart:fg1argadr3gdab3dgfsr41ag
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        //让Redis全部操作 cartKey 这个key
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);

        return operations;
    }
}
