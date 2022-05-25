package com.hyl.gulimall.seckill.scheduled;

import com.hyl.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 定时上架功能
 * 每晚3点上架最近三天的秒杀商品
 */

@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";


    @Scheduled(cron="* * * * * *")
    public void uploadSeckillLastest3Days() {
        log.info("上架秒杀的商品信息");
        //1、重复上架无需处理
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillLastest3Days();
        }finally {
            lock.unlock();
        }
    }
}
