package com.it.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.common.utils.R;
import com.it.gulimall.product.dao.SkuInfoDao;
import com.it.gulimall.product.entity.SkuImagesEntity;
import com.it.gulimall.product.entity.SkuInfoEntity;
import com.it.gulimall.product.entity.SpuInfoDescEntity;
import com.it.gulimall.product.feign.SeckillFeignService;
import com.it.gulimall.product.service.*;
import com.it.gulimall.product.vo.SeckillInfoVo;
import com.it.gulimall.product.vo.SkuItemSaleAttrVo;
import com.it.gulimall.product.vo.SkuItemVo;
import com.it.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    /**
     * key=&catelogId=0&brandId=0&min=0&max=0
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("sku_id", key).or().like("sku_name", key));
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }
        String minPrice = (String) params.get("min");
        String maxPrice = (String) params.get("max");
        if (!StringUtils.isEmpty(minPrice)) {
            try {
                BigDecimal min = new BigDecimal(minPrice);
                if (min.compareTo(new BigDecimal(0)) > 0) {
                    queryWrapper.ge("price", min);
                }
            } catch (Exception e) {
                log.error("------>", e);
            }

        }
        if (!StringUtils.isEmpty(maxPrice)) {
            try {
                BigDecimal max = new BigDecimal(maxPrice);
                if (max.compareTo(new BigDecimal(0)) > 0) {
                    queryWrapper.le("price", max);
                }
            } catch (Exception e) {
                log.error("------>", e);
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        return itemByAsync(skuId);
    }

    /**
     * 使用异步编排对代码进行优化。
     * 1和2可以同时进行。
     * 3，4，5需要等1获取结果再进行。
     * 所有任务完成才可以继续进行下一步
     */
    public SkuItemVo itemByAsync(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.sku的基本信息:”pms_sku_info“
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVo.setSkuInfo(skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((result) -> {
            //todo:3.获取spu的销售属性组合：
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(result.getSpuId());
            skuItemVo.setSkuItemSaleAttrs(saleAttrVos);
        }, threadPoolExecutor);
        CompletableFuture<Void> spuInfoDescFuture = infoFuture.thenAcceptAsync((result) -> {
            //4.获取spu的商品介绍:”pms_spu_info_desc“
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getSpuInfoDescBySpuId(result.getSpuId());
            skuItemVo.setSpuInfoDesc(spuInfoDesc);
        }, threadPoolExecutor);
        CompletableFuture<Void> attrGourpFuture = infoFuture.thenAcceptAsync((result) -> {
            //5.获取spu的规格参数属性值
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(result.getSpuId(), result.getCatalogId());
            skuItemVo.setSpuItemAttrGroups(attrGroupVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            //2.获取sku的图片信息:”pms_sku_images“
            List<SkuImagesEntity> skuImages = skuImagesService.getSkuImagesBySkuId(skuId);
            skuItemVo.setSkuImages(skuImages);
        }, threadPoolExecutor);

        //3.查询当前sku是否参与秒杀，如果参与秒杀，就封装秒杀信息
        CompletableFuture<Void> seckillTask = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo seckillInfoVo = r.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(seckillInfoVo);
            }
        }, threadPoolExecutor);
        //等所有任务做完：3,4,5一旦完成那么1一定就完成，可以省略。
        CompletableFuture.allOf(saleAttrFuture, spuInfoDescFuture, attrGourpFuture, imageFuture, seckillTask).get();
        return skuItemVo;
    }

    /**
     * 同步进行处理的代码
     */
    public SkuItemVo itemBySync(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1.sku的基本信息:”pms_sku_info“
        SkuInfoEntity skuInfo = getById(skuId);
        skuItemVo.setSkuInfo(skuInfo);
        Long spuId = skuInfo.getSpuId();
        Long catalogId = skuInfo.getCatalogId();
        //2.获取sku的图片信息:”pms_sku_images“
        List<SkuImagesEntity> skuImages = skuImagesService.getSkuImagesBySkuId(skuId);
        skuItemVo.setSkuImages(skuImages);
        //todo:3.获取spu的销售属性组合：
        List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(spuId);
        skuItemVo.setSkuItemSaleAttrs(saleAttrVos);
        //4.获取spu的商品介绍:”pms_spu_info_desc“
        SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getSpuInfoDescBySpuId(spuId);
        skuItemVo.setSpuInfoDesc(spuInfoDesc);
        //5.获取spu的规格参数属性值
        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
        skuItemVo.setSpuItemAttrGroups(attrGroupVos);
        return skuItemVo;
    }

}