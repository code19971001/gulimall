package com.it.gulimall.cart.vo;

import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车的内容:需要计算的属性，必须重写get方法，保证每次获取属性的时候都会进行计算。
 *
 * @author : code1997
 * @date : 2021/7/4 21:58
 */
@ToString
public class Cart {

    private List<CartItem> cartItems;
    private Integer countNum;
    private Integer countType;
    /**
     * 商品总价格
     */
    private BigDecimal totalAmount;
    /**
     * 减免价格
     */
    private BigDecimal reducePrice = new BigDecimal("0.00");

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public Integer getCountNum() {
        countNum = 0;
        if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem cartItem : cartItems) {
                countNum += cartItem.getCount();
            }
        }
        return countNum;
    }

    public Integer getCountType() {
        countType = 0;
        if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem cartItem : cartItems) {
                countType += 1;
            }
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal price = new BigDecimal("0.00");
        //1.计算购物项总价
        if (cartItems != null && !cartItems.isEmpty()) {
            for (CartItem cartItem : cartItems) {
                if (cartItem.isChecked()) {
                    price = price.add(cartItem.getTotalPrice());
                }
            }
        }
        //2.减去优惠价价格
        return price.subtract(getReducePrice());
    }


    public BigDecimal getReducePrice() {
        return reducePrice;
    }

}
