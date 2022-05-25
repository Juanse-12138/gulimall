package com.hly.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.hly.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

//    @Test
//    public void searchData() throws IOException {
//        SearchRequest searchRequest = new SearchRequest();
//        searchRequest.indices("bank");
//    }

    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User();
        String s = JSON.toJSONString(user);
        indexRequest.source(s, XContentType.JSON);

        //执行保存操作
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
    }

    @Data
    class User{
        String username;
        int age;
        boolean gender;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }



}
