package com.it.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/6/10 1:04
 */
@Data
public class SkuItemSaleAttrVo {

    /**
     * 属性id
     */
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;

    private List<AttrValueWithSkuIdVo> attrValues;


}