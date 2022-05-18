package com.hyl.gulimall.cart.config;

import com.hyl.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author hyl_marco
 * @data 2022/5/15 - 18:06
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    public void addInterceptors(InterceptorRegistry registry){
        //配置需要使用的拦截器、拦截器的匹配规则
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
