package com.it.gulimall.search.controller;

import com.it.common.exception.BizCodeEnume;
import com.it.common.to.es.SkuEsModel;
import com.it.common.utils.R;
import com.it.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/20 21:58
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = true;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (Exception e) {
            log.error("Es商品上架出现异常：{}", e);
            R.error(BizCodeEnume.PRODUCT_SAVE.getCode(), BizCodeEnume.PRODUCT_SAVE.getMsg());
        }
        return b ? R.ok() : R.error(BizCodeEnume.PRODUCT_SAVE.getCode(), BizCodeEnume.PRODUCT_SAVE.getMsg());
    }


}
