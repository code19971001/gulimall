package com.it.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.gulimall.product.dao.SkuImagesDao;
import com.it.gulimall.product.entity.SkuImagesEntity;
import com.it.gulimall.product.service.SkuImagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuImages(List<SkuImagesEntity> imagesEntities) {
        this.saveBatch(imagesEntities);
    }

    @Override
    public List<SkuImagesEntity> getSkuImagesBySkuId(Long skuId) {
        return this.baseMapper.selectList(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
    }

}