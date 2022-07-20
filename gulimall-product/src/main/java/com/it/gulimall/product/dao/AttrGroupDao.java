package com.it.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.gulimall.product.entity.AttrGroupEntity;
import com.it.gulimall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 01:36:30
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
