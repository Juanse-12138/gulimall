package com.hyl.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author hyl_marco
 * @data 2022/4/19 - 0:23
 */
@Configuration
public class MyRedissonConfig {
    /**
     *
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        //集群模式
        //config.useClusterServers().addNodeAddress("127.0.0.1:7004","127.0.0.1:7001");
        //单节点模式
        /*这里必须加上redis://*/
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
        return Redisson.create(config);
    }
}
