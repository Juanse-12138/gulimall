package com.hyl.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/*
* 1.整合Mybatis-plus
* （1）导入依赖
* （2）配置
*       ①配置数据源（数据库驱动 在application.yml文件中配置数据源信息）
*       ②配置MyBatis-Plus
*           使用MapperScan注解 指示Mapper接口所在位置
*           指示sql映射文件的位置
* */
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.hyl.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.hyl.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
