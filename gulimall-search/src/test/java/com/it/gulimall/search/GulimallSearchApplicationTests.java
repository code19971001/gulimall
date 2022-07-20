package com.it.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.it.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 索引数据到es中去。
     */
    @Test
    public void testIndexData() throws IOException {
        System.out.println("初始化es客户端：" + restHighLevelClient);
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        Student student = new Student("张三", "男", 23);
        JSON.toJSONString(student);
        indexRequest.source(student, XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    /**
     * 测试查询数据
     * "_index" : "bank",
     * "_type" : "account",
     * "_id" : "345",
     * "_score" : 5.4032025,
     * "_source" : {
     */
    @Test
    public void testSearchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //构造检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("address", "mill");
        searchSourceBuilder.query(queryBuilder);
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);
        System.out.println(searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);
        //进行检索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse);
        //获取命中的记录
        SearchHit[] hits = searchResponse.getHits().getHits();
        Arrays.stream(hits).map(hit -> JSON.parseObject(hit.getSourceAsString(),Account.class)).forEach(System.out::println);
        ((Terms)searchResponse.getAggregations().get("ageAgg")).getBuckets().forEach(bucket -> {
            System.out.println(bucket.getKeyAsString());
        });
        System.out.println(((Avg) searchResponse.getAggregations().get("balanceAvg")).getValueAsString());

    }

    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @AllArgsConstructor
    @Data
    static class Student implements Serializable {
        private String username;
        private String gender;
        private Integer age;
    }

}
