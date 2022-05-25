package com.hyl.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author hyl_marco
 * @data 2022/3/26 - 17:33
 */
@EnableTransactionManagement
@MapperScan("com.hyl.gulimall.ware.dao")
@Configuration
public class WareMyBatisConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        paginationInterceptor.setOverflow(true);

        paginationInterceptor.setLimit(1000);

        return paginationInterceptor;
    }
}
