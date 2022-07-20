package com.it.gulimall.ware.dao;

import com.it.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * 
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:52:47
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);

    /**
     * 查找有库存的商品的id
     */
    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);

    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("lockCount") Long lockCount);

    void unlockSkuStock(@Param("skuId") Long skuId,@Param("wareId") Long wareId,@Param("count") Integer count);

}
