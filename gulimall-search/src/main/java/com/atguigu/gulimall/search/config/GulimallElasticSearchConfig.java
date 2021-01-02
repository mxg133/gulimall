package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 孟享广
 * @date 2021-01-02 11:41 上午
 * @description
 * 1 导入依赖
 * 2 编写配置 给容器中注入一个RestHighLevelClient
 * 3 参API
 */

@Configuration
public class GulimallElasticSearchConfig {

    @Bean
    public RestHighLevelClient esRestClient() {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("10.211.55.3", 9200, "http")
//                        new HttpHost("localhost", 9201, "http")
        );
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
