package com.it.gulimall.search.service;

import com.it.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/20 22:01
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
