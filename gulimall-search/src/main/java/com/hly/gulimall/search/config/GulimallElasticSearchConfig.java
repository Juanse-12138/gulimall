package com.hly.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 整合es
 * 1、导入依赖
 * 2、编写配置，给容器注入一个RestHighLevelClient
 * 3、参照api文档进行操作 https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
 */
@SpringBootConfiguration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder=RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS=builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {
        RestClientBuilder builder=null;
        builder=RestClient.builder(new HttpHost("192.168.50.1",9200,"http"));
        RestHighLevelClient client=new RestHighLevelClient(builder);
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(new HttpHost("192.168.50.1",9200,"http"))
//        );
        return client;
    }
}
