package com.it.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.it.common.constant.CartConstant;
import com.it.common.utils.R;
import com.it.gulimall.cart.feign.ProductFeignService;
import com.it.gulimall.cart.interceptor.CartInterceptor;
import com.it.gulimall.cart.service.CartService;
import com.it.gulimall.cart.vo.Cart;
import com.it.gulimall.cart.vo.CartItem;
import com.it.gulimall.cart.vo.SkuInfoVo;
import com.it.gulimall.cart.vo.UserInfoTo;
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
import java.util.stream.Collectors;

/**
 * @author : code1997
 * @date : 2021/7/4 22:30
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;


    @Override
    public CartItem addToCart(Long skuId, Integer number) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String redisCart = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(redisCart)) {
            CartItem cartItem = new CartItem();
            //远程查询当前要添加的商品信息
            CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.info(skuId);
                SkuInfoVo skuInfoVo = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setSkuId(skuId);
                cartItem.setChecked(true);
                cartItem.setCount(number);
                cartItem.setImage(skuInfoVo.getSkuDefaultImg());
                cartItem.setTitle(skuInfoVo.getSkuTitle());
                cartItem.setPrice(skuInfoVo.getPrice());
            }, threadPoolExecutor);
            CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                List<String> saleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(saleAttrValues);
            }, threadPoolExecutor);
            CompletableFuture.allOf(task1, task2).get();
            //将商品添加到购物车
            String jsonStr = JSON.toJSONString(cartItem);
            cartOps.put(String.valueOf(skuId), jsonStr);
            return cartItem;
        } else {
            CartItem cartItem = JSON.parseObject(redisCart, CartItem.class);
            cartItem.setCount(cartItem.getCount() + number);
            String jsonStr = JSON.toJSONString(cartItem);
            cartOps.put(String.valueOf(skuId), jsonStr);
            return cartItem;
        }
    }

    /**
     * 获取购物车中某个购物项
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String redisCart = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(redisCart, CartItem.class);
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        //1.快速得到用户信息:使用ThreadLocal.
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            //已经登陆
            String cartKey = CartConstant.CART_PREFIX + ":" + userInfoTo.getUserKey();
            //需要合并临时购物车
            List<CartItem> tempCartItems = getCartItems(cartKey);
            if (tempCartItems != null) {
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                //清除临时购物车的数据
                clearCart(cartKey);
            }
            //获取登陆后的购物车数据::包含临时购物车数据和账户购物车的数据
            cartKey = CartConstant.CART_PREFIX + ":" + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setCartItems(cartItems);
        } else {
            //没登陆
            String cartKey = CartConstant.CART_PREFIX + ":" + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setCartItems(cartItems);
        }
        return cart;
    }

    /**
     * 根据cartKey清空购物车数据.
     *
     * @param cartKey : cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 勾选购物项的选中状态
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setChecked(check == 1);
        String jsonStr = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), jsonStr);

    }

    @Override
    public void changeItemCount(Long skuId, Integer number) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(number);
        String jsonStr = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), jsonStr);
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 使用feign进行远程调用会导致请求头信息的丢失。
     */
    @Override
    public List<CartItem> getCurUserCartItems() {
        UserInfoTo infoTo = CartInterceptor.threadLocal.get();
        if (infoTo.getUserId() == null) {
            return null;
        }
        //需要更新购物车的价格
        //todo:更新redis中购物车商品项的价格
        return getCartItems(CartConstant.CART_PREFIX + ":" + infoTo.getUserId()).stream()
                .filter(CartItem::isChecked)
                .map(cartItem -> {
                    cartItem.setPrice(productFeignService.getPrice(cartItem.getSkuId()));
                    return cartItem;
                }).collect(Collectors.toList());
    }

    protected List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();

        if (values != null && !values.isEmpty()) {
            List<CartItem> itemList = values.stream().map((obj) -> {
                String jsonStr = (String) obj;
                return JSON.parseObject(jsonStr, CartItem.class);
            }).collect(Collectors.toList());
            return itemList;
        }
        return null;
    }

    /**
     * 获取到要操作的购物车
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey;
        if (StringUtils.isEmpty(userInfoTo.getUserId())) {
            //临时购物车
            cartKey = CartConstant.CART_PREFIX + ":" + userInfoTo.getUserKey();
        } else {
            //用户的购物车
            cartKey = CartConstant.CART_PREFIX + ":" + userInfoTo.getUserId();
        }
        return stringRedisTemplate.boundHashOps(cartKey);
    }
}
