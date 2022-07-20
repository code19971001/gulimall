package com.it.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/6/10 0:54
 */
@Data
public class SpuItemAttrGroupVo {

    /**
     * 属性id
     */
    private String groupName;

    private List<SpuItemBaseAttrVo> spuItemBaseAttrs;
}