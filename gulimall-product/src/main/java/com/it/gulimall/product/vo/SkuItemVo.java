package com.it.gulimall.product.vo;

import com.it.gulimall.product.entity.SkuImagesEntity;
import com.it.gulimall.product.entity.SkuInfoEntity;
import com.it.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * //1.sku的基本信息:”pms_sku_info“
 * //2.获取sku的图片信息:”pms_sku_images“
 * //3.获取spu的销售属性组合：
 * //4.获取spu的商品介绍:”pms_spu_info_desc“
 * //5.获取spu的规格参数属性值
 *
 * @author : code1997
 * @date : 2021/6/9 23:20
 */
@Data
public class SkuItemVo {

    SkuInfoEntity skuInfo;

    boolean hasStock = true;

    List<SkuImagesEntity> skuImages;

    List<SkuItemSaleAttrVo> skuItemSaleAttrs;

    SpuInfoDescEntity spuInfoDesc;

    List<SpuItemAttrGroupVo> spuItemAttrGroups;

    /**
     * 当前商品的秒杀优惠信息
     */
    SeckillInfoVo seckillInfo;




}
