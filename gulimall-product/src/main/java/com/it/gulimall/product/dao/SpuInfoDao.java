package com.it.gulimall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.it.gulimall.product.entity.SpuInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 01:36:29
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Long spuId,@Param("status") Integer status);
}
