package com.it.gulimall.product.vo;

import lombok.Data;

/**
 * Vo:View Object:和页面打交道的对象，对于实体类Entity对象来说，我们随意的修改其属性是不对的,在这个时候可以采用创建VO对象
 *
 * @author : code1997
 * @date : 2021/5/6 23:09
 */
@Data
public class AttrVo {

    private static final long serialVersionUID = 1L;

    /**
     * 属性id
     */
    private Long attrId;
    /**
     * 属性名
     */
    private String attrName;
    /**
     * 是否需要检索[0-不需要，1-需要]
     */
    private Integer searchType;
    /**
     * 属性图标
     */
    private String icon;
    /**
     * 可选值列表[用逗号分隔]
     */
    private String valueSelect;
    /**
     * 属性类型[0-销售属性，1-基本属性，2-既是销售属性又是基本属性]
     */
    private Integer attrType;
    /**
     * 启用状态[0 - 禁用，1 - 启用]
     */
    private Long enable;
    /**
     * 所属分类
     */
    private Long catelogId;
    /**
     * 快速展示【是否展示在介绍上；0-否 1-是】，在sku中仍然可以调整
     */
    private Integer showDesc;
    /**
     * 是否允许多选【0-否 1-是】
     */
    private Integer valueType;

    private Long attrGroupId;
}
