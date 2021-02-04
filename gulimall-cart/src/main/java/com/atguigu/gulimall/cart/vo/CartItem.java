package com.atguigu.gulimall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 孟享广
 * @date 2021-02-03 12:33 下午
 * @description 购物车里的每一个购物项
 * @Data 不用的原因
 * 需要计算的属性必须重新get()的方法，这样才能保证每一次属性都会进行重新计算
 */
//为什么不用@Data 因为要自定义一个方法getTotalPrice()
//@Data
public class CartItem {

    private Long skuId;
    //是否被选中
    private Boolean check = true;
    private String title;
    private String image;
    //套餐信息
    private List<String> skuAttr;
    //涉及到计算 必须用BigDecimal
    private BigDecimal price;
    private Integer count;
    //总价
    private BigDecimal totalPrice;

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 计算每一个购物项的总价
     */
    public BigDecimal getTotalPrice() {

        return this.price.multiply(new BigDecimal(this.count + ""));
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
