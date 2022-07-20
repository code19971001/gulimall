package com.it.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.it.common.constant.LoggerConstant;
import com.it.common.to.es.SkuEsModel;
import com.it.common.utils.R;
import com.it.gulimall.search.config.GulimallElasticSearchConfig;
import com.it.gulimall.search.constant.EsConstant;
import com.it.gulimall.search.feign.ProductFeignService;
import com.it.gulimall.search.service.MallSearchService;
import com.it.gulimall.search.vo.AttrResponseVo;
import com.it.gulimall.search.vo.BrandVo;
import com.it.gulimall.search.vo.SearchParams;
import com.it.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : code1997
 * @date : 2021/5/31 21:04
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParams params) {
        SearchRequest searchRequest = buildSearchRequest(params);
        SearchResult searchResult = null;
        try {
            //执行检索请求
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            log.info(LoggerConstant.LOGGER_PREFIX + "查询响应的结果:" + searchResponse);
            //封装响应数据
            searchResult = buildSearchResponse(searchResponse, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 封装响应数据
     * searchResult.setAttrs();
     * searchResult.setBrands();
     * searchResult.setCatalogVos();
     * searchResult.setProducts();
     * searchResult.setTotal();  √
     * searchResult.setPageNum();  √
     * searchResult.setTotalPages();  √
     *
     * @return ： SearchResponse object
     */
    private SearchResult buildSearchResponse(SearchResponse searchResponse, SearchParams params) {
        SearchResult searchResult = new SearchResult();
        SearchHits searchHits = searchResponse.getHits();
        //封装分页信息
        long totalSize = searchHits.getTotalHits().value;
        searchResult.setTotal(totalSize);
        searchResult.setTotalPages((int) (totalSize % EsConstant.PRODUCT_PAGE_SIZE == 0 ? totalSize / EsConstant.PRODUCT_PAGE_SIZE : totalSize / EsConstant.PRODUCT_PAGE_SIZE + 1));
        searchResult.setPageNum(params.getPageNum());
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= searchResult.getTotalPages(); i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        SearchHit[] hits = searchHits.getHits();
        //封装product信息
        ArrayList<SkuEsModel> skuEsModels = new ArrayList<>();
        for (SearchHit hit : hits) {
            SkuEsModel skuEsModel = JSON.parseObject(hit.getSourceAsString(), SkuEsModel.class);
            System.out.println(JSON.toJSONString(skuEsModel));
            //设置高亮显示
            if (!StringUtils.isEmpty(params.getKeyword())) {
                HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                String highlightStr = highlightField.fragments()[0].toString();
                skuEsModel.setSkuTitle(highlightStr);
            }
            skuEsModels.add(skuEsModel);
        }
        searchResult.setProducts(skuEsModels);
        Aggregations aggregations = searchResponse.getAggregations();
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> catalogAggBuckets = catalogAgg.getBuckets();
        ArrayList<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalogAggBuckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            long catalogId = Long.parseLong(bucket.getKeyAsString());
            catalogVo.setCatalogId(catalogId);
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            List<? extends Terms.Bucket> catalogNameAggBuckets = catalogNameAgg.getBuckets();
            catalogVo.setCatalogName(catalogNameAggBuckets.get(0).getKeyAsString());
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);

        //封装brands信息
        ParsedLongTerms brandAggs = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brandAggBuckets = brandAggs.getBuckets();
        ArrayList<SearchResult.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brandAggBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(Long.parseLong(bucket.getKeyAsString()));
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            List<? extends Terms.Bucket> brandImgAggBuckets = brandImgAgg.getBuckets();
            brandVo.setBrandImg(brandImgAggBuckets.get(0).getKeyAsString());
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            List<? extends Terms.Bucket> brandNameAggBuckets = brandNameAgg.getBuckets();
            brandVo.setBrandName(brandNameAggBuckets.get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);

        //封装attr信息
        ArrayList<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAggs = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAggs.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = Long.parseLong(bucket.getKeyAsString());
            attrVo.setAttrId(attrId);
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            attrVo.setAttrValue(attrValueAgg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList()));
            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);
        //构建面包屑导航数据
        if (params.getAttrs() != null && !params.getAttrs().isEmpty()) {
            List<SearchResult.NavVo> navVos = params.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] attrParam = attr.split("_");
                navVo.setNavValue(attrParam[1]);
                R r = productFeignService.attrInfo(Long.valueOf(attrParam[0]));
                searchResult.getAttrIds().add(Long.valueOf(attrParam[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo attrResponseVo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(attrResponseVo.getAttrName());
                } else {
                    //如果远程调用失败，就默认属性的名字为id
                    navVo.setNavName(attrParam[0]);
                }
                //取消面包屑之后，我们要跳转到的地方，将请求地址的url里面的当前值制空，拿到所有的查询条件去掉当前的attr。
                String replaceUrl = replaceQueryString(params, "attrs", attr);
                navVo.setLink("http://search.gulimall.com/list.html?" + replaceUrl);
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVos);
        }
        if (params.getBrandId() != null && !params.getBrandId().isEmpty()) {
            List<SearchResult.NavVo> navs = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.brandInfo(params.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brandVo2s = r.getData("brands", new TypeReference<List<BrandVo>>() {
                });
                StringBuilder builder = new StringBuilder();
                String replace = "";
                for (BrandVo brandVo : brandVo2s) {
                    builder.append(brandVo.getName()).append(";");
                    replace = replaceQueryString(params, "brandId", brandVo.getBrandId() + "");
                }
                navVo.setNavValue(builder.substring(0, builder.toString().length() - 1));
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }
        //todo:分类信息，不需要导航取消功能，也就是不需要设置link

        return searchResult;
    }

    private String replaceQueryString(SearchParams params, String key, String attr) {
        String encodeAttr = null;
        try {
            //因为默认会对中文进行URL编码，因此我们替换的时候也需要对我们的attr进行编码
            //注意：前端会将空格编码为20%，java会将空格编码为”+“，因此我们需要手动进行替换
            encodeAttr = URLEncoder.encode(attr, "utf-8").replace("+", "20%");
        } catch (UnsupportedEncodingException e) {
            log.error(LoggerConstant.LOGGER_PREFIX + e.getMessage(), e);
        }
        return params.getQueryString().replace("&" + key + "=" + encodeAttr, "");
    }

    /**
     * 封装检索请求的方法
     *
     * @return ：SearchRequest object
     */
    private SearchRequest buildSearchRequest(SearchParams params) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(params.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", params.getKeyword()));
        }
        if (params.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", params.getCatalog3Id()));
        }
        if (params.getBrandId() != null && !params.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", params.getBrandId()));
        }
        //--注意这里的nested的实现方式及
        List<String> paramsAttrs = params.getAttrs();
        if (paramsAttrs != null && !paramsAttrs.isEmpty()) {
            //--attrs=1_5寸:8寸&attrs=2_16G:8G
            for (String paramsAttr : paramsAttrs) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                String[] attrParam = paramsAttr.split("_");
                boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrParam[0]));
                boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrParam[1].split(":")));
                boolQuery.filter(QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None));
            }
        }
        if (params.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termsQuery("hasStock", params.getHasStock() > 0));
        }
        String price = params.getSkuPrice();

        if (!StringUtils.isEmpty(price)) {
            String[] skuPrice = price.split("_");
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            if (skuPrice.length == 2) {
                rangeQuery.gte(skuPrice[0]).lte(skuPrice[1]);
            } else if (skuPrice.length == 1) {
                if (price.startsWith("_")) {
                    rangeQuery.lte(skuPrice[0]);
                }
                if (price.endsWith("_")) {
                    rangeQuery.gte(skuPrice[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        searchSourceBuilder.query(boolQuery);
        //2.排序，分页，高亮
        String sortStr = params.getSort();
        if (!StringUtils.isEmpty(sortStr)) {
            String[] sorts = sortStr.split("_");
            searchSourceBuilder.sort(sorts[0], "ASC".equalsIgnoreCase(sorts[1]) ? SortOrder.ASC : SortOrder.DESC);
        }
        Integer pageNum = params.getPageNum();
        searchSourceBuilder.from((pageNum - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);
        if (!StringUtils.isEmpty(params.getKeyword())) {
            searchSourceBuilder.highlighter(new HighlightBuilder().field("skuTitle").preTags("<b style='color:red'>").postTags("</b>"));
        }

        //3.构建聚合
        //3.1 聚合品牌信息
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        //3.2 聚合分类信息
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        //3.3 聚合属性信息
        NestedAggregationBuilder attrNestedAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(1);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrNestedAgg.subAggregation(attrIdAgg);

        searchSourceBuilder.aggregation(brandAgg);
        searchSourceBuilder.aggregation(catalogAgg);
        searchSourceBuilder.aggregation(attrNestedAgg);
        log.info(LoggerConstant.LOGGER_PREFIX + "构建的dsl语句:" + searchSourceBuilder);
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);

    }


}
