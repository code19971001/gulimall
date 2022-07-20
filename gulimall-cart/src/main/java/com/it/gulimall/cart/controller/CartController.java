package com.it.gulimall.cart.controller;

import com.it.gulimall.cart.service.CartService;
import com.it.gulimall.cart.vo.Cart;
import com.it.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * todo:购物车全选，全不选的设置。
 * @author : code1997
 * @date : 2021/7/4 19:26
 */
@Controller
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/curUserCartItems")
    public List<CartItem> getCurUserCartItems(){
        System.out.println("执行到了");
        List<CartItem> curUserCartItems = cartService.getCurUserCartItems();
        System.out.println(curUserCartItems);
        return curUserCartItems;
    }

    /**
     * 浏览器有一个cookie:user-key;标识用户肚饿身份，以恶搞月后过期；
     * 如果第一次使用jd的购物车功能，都会给一个连是的用户身份，浏览器进行保存，每次访问的时候都会带上这个cookie。
     * 使用拦截器来做：
     * 如果登陆：session存在
     * 没有登陆：按照cookie里面带的user-key来做。
     * 第一次使用：如果没有临时用户，需要帮忙创建一个临时用户。
     */
    @GetMapping({"/cart.html"})
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        System.out.println(cart);
        model.addAttribute("cart", cart);
        return "cartList";
    }

    /**
     * 注意使用重定向,否则会存在刷新还会添加的情况的出现。
     *
     * attributes.addAttribute("skuId",skuId);
     * attributes.addFlashAttribute()
     *
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("number") Integer number, RedirectAttributes attributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, number);
        attributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/cart/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId , Model model) {
        //重定向到成功页面，再次查询购物车数据即可。
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("cartItem", cartItem);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("check")Integer check){
        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId")Long skuId,@RequestParam("num")Integer number){
        cartService.changeItemCount(skuId,number);
        return "redirect:http://cart.gulimall.com/cart/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart/cart.html";
    }
}
