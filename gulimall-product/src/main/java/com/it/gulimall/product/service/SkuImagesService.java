package com.it.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.common.utils.PageUtils;
import com.it.gulimall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 01:36:29
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuImages(List<SkuImagesEntity> imagesEntities);

    List<SkuImagesEntity> getSkuImagesBySkuId(Long skuId);
}

