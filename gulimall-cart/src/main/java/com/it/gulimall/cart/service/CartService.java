package com.it.gulimall.cart.service;

import com.it.gulimall.cart.vo.Cart;
import com.it.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author : code1997
 * @date : 2021/7/4 22:30
 */
public interface CartService {

    CartItem addToCart(Long skuId, Integer number) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void changeItemCount(Long skuId, Integer number);

    void deleteItem(Long skuId);

    List<CartItem> getCurUserCartItems();

}
