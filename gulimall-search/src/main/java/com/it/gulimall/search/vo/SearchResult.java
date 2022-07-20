package com.it.gulimall.search.vo;

import com.it.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/31 21:31
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页面
     */
    private Integer pageNum;

    /**
     * 添加当前显示的页码信息
     */
    private List<Integer> pageNavs;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 当前查询结果，所有涉及到的品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询结果，所有涉及到的分类信息。
     */
    private List<CatalogVo> catalogs;

    /**
     * 查询结果中所涉及的所有属性的集合
     */
    private List<AttrVo> attrs;

    /**
     * 前端面包屑导航数据的展示
     */
    private List<NavVo> navs = new ArrayList<>();

    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

}
