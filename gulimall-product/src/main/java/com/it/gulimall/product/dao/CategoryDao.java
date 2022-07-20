package com.it.gulimall.product.dao;

import com.it.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 01:36:30
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
