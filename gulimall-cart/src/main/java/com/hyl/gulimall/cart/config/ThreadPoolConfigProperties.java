package com.hyl.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * @author hyl_marco
 * @data 2022/5/2 - 22:03
 *
 * 用于放置线程池的配置
 */
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {
    private Integer coresize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
