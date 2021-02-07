package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

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
     * 获取购物车的一个购物项
     */
    @Override
    public CartItem getCartItem(Long skuId) {

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String str = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(str, CartItem.class);

        return cartItem;
    }

    /**
     * 跳到购物车列表，并检测是否需要合并
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        //要返回的大对象
        Cart cart = new Cart();

        //1 先看是否有用户登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //说明用户登录
            String CartKey = CART_PREFIX + userInfoTo.getUserId();
            //看一下有没有临时用户的购物车
            String tempCartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取购物车里的购物项目
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            if (tempCartItems != null && tempCartItems.size() > 0) {
                //说明临时用户购物车有数据 需要合并
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                //合并完成，需要清除临时用户
                clearCart(tempCartKey);
            }
            //再来获取登录后的购物车数据 大合并
            List<CartItem> cartItems = getCartItems(CartKey);
            cart.setItems(cartItems);

        } else {
            //未登录 临时用户
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            //获取临时购物车里的购物项目
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }

        return cart;
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

    /**
     * 获取购物车里的所有购物项目
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        //从Redis拿数据
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> collect = values.stream().map((obj) -> {
                String str = (String) obj;
                CartItem cartItem = JSON.parseObject(str, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clearCart(String cartKey) {

        redisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项目
     */
    @Override
    public void checkItem(Long skuId, Integer check) {

        //获取到我们要操作的购物车
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //获取购物车的一个购物项
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        String str = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), str);
    }

    /**
     * 修改购物项目的数量
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {

        //获取到我们要操作的购物车
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //获取购物车的一个购物项
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        String str = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), str);
    }

    /**
     * 删除购物车里的某一项
     */
    @Override
    public void deleteItem(Long skuId) {

        //获取到我们要操作的购物车
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItem> getUserCartItems() {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            //没登录
            return null;
        } else {
            //登录了
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItemList = getCartItems(cartKey);
            //筛选被选中的购物项 过滤数据
            List<CartItem> collect = cartItemList.stream()
                    .filter(item -> item.getCheck())
                    .map((item) -> {
                //一定要获取到最新的价格
                R r = productFeignService.getPrice(item.getSkuId());
                String data = (String) r.get("data");
                item.setPrice(new BigDecimal(data));
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
    }
}
