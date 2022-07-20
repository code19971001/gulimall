package com.it.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面可以能传递过来的查询条件：
 *  catalog3Id=225&keyword=小米&sort=saleCount_asc&hasStock=0&brandId=1&brandId=2&attrs=1_安卓:安卓&attrs=2_5寸:6寸
 * 根据三级分类进入：catalog3Id
 * 搜索框关键字进入：keyword
 * 全文检索字段：
 * 排序字段：
 * 过滤：hasStock,skuPrice,brandId,catalog3Id,attrs
 * @author : code1997
 * @date : 2021/5/31 21:01
 */
@Data
public class SearchParams {

    private String keyword;

    private Long catalog3Id;

    /**
     * sort=saleCount_asc||sort=saleCount_desc
     * sort=skuPrice_asc||sort=skuPrice_desc
     * sort=hotScore_asc||sort=hotScore_desc
     */
    private String sort;

    /**
     * 是否只显示有货
     */
    private Integer hasStock;

    /**
     * 价格区间
     */
    private String skuPrice;

    /**
     * 根据品牌id进行查询，可以多选。
     */
    private List<Long> brandId;

    /**
     * 封装属性
     */
    private List<String> attrs;

    /**
     * 封装页码
     */
    private Integer pageNum = 1;

    /**
     * 封装原生的查询条件
     */
    private String queryString;

}
