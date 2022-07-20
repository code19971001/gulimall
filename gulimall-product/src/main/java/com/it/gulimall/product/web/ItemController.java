package com.it.gulimall.product.web;

import com.it.gulimall.product.service.SkuInfoService;
import com.it.gulimall.product.vo.SkuItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author : code1997
 * @date : 2021/6/9 22:59
 */
@Controller
@Slf4j
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 获取当前sku的详情
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo= skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
