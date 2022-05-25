package com.hyl.gulimall.seckill.service;

import com.hyl.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

public interface SeckillService {
    void uploadSeckillLastest3Days();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSecKillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
