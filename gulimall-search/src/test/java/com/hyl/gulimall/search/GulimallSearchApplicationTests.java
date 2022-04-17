package com.hyl.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.hyl.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.hyl.gulimall.search.config.GulimallElasticSearchConfig.COMMON_OPTIONS;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }


    /*index保存更新二合一*/
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
//        indexRequest.source("username", "hyl", "age", 18, "gender", "男");
        User user = new User();
        user.setUserName("hyl");
        user.setAge(18);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);

        IndexResponse index = client.index(indexRequest, COMMON_OPTIONS);

        System.out.println(index);
    }

    @Test
    public void searchData() throws IOException{
        /*1.创建检索请求*/
        SearchRequest searchRequest = new SearchRequest();
        /*指定索引*/
        searchRequest.indices("bank");
        /*指定DSL，检索条件*/
        /*SearchSourceBuilder sourceBuilder封装条件*/
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /*构造检索条件*/
//        searchSourceBuilder.query();
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
//        searchSourceBuilder.aggregation();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        /*按照年龄分布进行聚合*/
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        /*计算平均薪资*/
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        System.out.println(searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);

        /*2.执行检索*/
        SearchResponse search = client.search(searchRequest, COMMON_OPTIONS);

        /*分析结果*/
        System.out.println(search.toString());
    }

    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;
    }

}
