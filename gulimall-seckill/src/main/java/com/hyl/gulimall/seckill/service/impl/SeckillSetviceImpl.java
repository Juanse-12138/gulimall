package com.hyl.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hyl.common.to.mq.SeckillOrderTo;
import com.hyl.common.utils.R;
import com.hyl.common.vo.MemberRespVo;
import com.hyl.gulimall.seckill.LoginUserInterceptor;
import com.hyl.gulimall.seckill.feign.CouponFeignService;
import com.hyl.gulimall.seckill.feign.ProductFeignService;
import com.hyl.gulimall.seckill.service.SeckillService;
import com.hyl.gulimall.seckill.to.SeckillSkuRedisTo;
import com.hyl.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.hyl.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillSetviceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Override
    public void uploadSeckillLastest3Days() {
        //1、数据库扫描需要上架的商品
        R session=couponFeignService.getLastest3DaysSession();
        if(session.getCode()==0) {
            List<SeckillSessionsWithSkus> data=session.getData(new TypeReference<List<SeckillSessionsWithSkus>>(){});
            //将信息缓存到redis
            //1、缓存活动信息
            saveSessionInfo(data);
            //2、缓存活动关联商品信息
            saveSessionSkuInfo(data);
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        Set<String> keys=redisTemplate.keys(SESSIONS_CACHE_PREFIX+"*");
        long currentTime = System.currentTimeMillis();
        for(String key:keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            long startTime = Long.parseLong(split[0]);
            long endTime = Long.parseLong(split[1]);
            //当前秒杀活动处于有效期内
            if(currentTime>startTime&&currentTime<endTime) {
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                assert range!=null;
                List<String> strings = hashOps.multiGet(range);
                if(!CollectionUtils.isEmpty(strings)) {
                    return strings.stream().map(item->JSON.parseObject(item,SeckillSkuRedisTo.class)).collect(Collectors.toList());
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSecKillInfo(Long skuId) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys=ops.keys();
        if(null!=keys) {
            String regx="\\d_"+skuId;
            for(String key:keys) {
                if(Pattern.matches(regx,key)) {
                    String json = ops.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    long current = System.currentTimeMillis();
                    if(current>=redisTo.getStartTime()&&current<=redisTo.getEndTime()) {

                    } else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        long l1 = System.currentTimeMillis();
        MemberRespVo member = LoginUserInterceptor.loginUser.get();
        //1、获取当前商品的秒杀信息
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String s = ops.get(killId);
        if(!StringUtils.isEmpty(s)) {
            SeckillSkuRedisTo redisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
            long current = System.currentTimeMillis();
            //检验时间合法性
            if(current>=redisTo.getStartTime()&&current<=redisTo.getEndTime()) {
                String randomCode = redisTo.getRandomCode();
                String id=redisTo.getPromotionSessionId()+"_"+redisTo.getSkuId();
                long ttl = redisTo.getEndTime() - current;
                if(randomCode.equals(key)&&killId.equals(id)) {
                    //验证购物数量
                    if(num<=redisTo.getSeckillLimit()) {
                        //验证这个人是否已经购买过
                        //幂等性 如果秒杀成功就在redis标识该用户已购买
                        //SETNX
                        String userKey=member.getId()+"_"+redisTo.getPromotionSessionId()+"_"+redisTo.getSkuId();
                        //设置超时时间，如果秒杀场次结束，那么数据自动清空
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(userKey,num.toString(),ttl, TimeUnit.MILLISECONDS);
                        if(aBoolean) {
                            //占位成功，说明该用户未购买
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            try {
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                if(b) {
                                    //秒杀成功 快速下单  发送mq消息
                                    String orderSn = IdWorker.getTimeId().substring(0, 32);
                                    SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                    seckillOrderTo.setOrderSn(orderSn);
                                    seckillOrderTo.setMemberId(member.getId());
                                    seckillOrderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                    seckillOrderTo.setSkuId(redisTo.getSkuId());
                                    seckillOrderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                    seckillOrderTo.setNum(num);
                                    rabbitTemplate.convertAndSend("order-event-exchange","order.seckill.order",seckillOrderTo);
                                    long l2 = System.currentTimeMillis();
                                    log.info("秒杀成功，用时"+(l2-l1)+"毫秒");
                                    return orderSn;
                                }
                            } catch (InterruptedException e) {
                                return null;
                            }
                        }
//                        else {
//                            //该用户购买过，判断当前该用户购买总量是否超过limit
//                        }
                    }
                }
            }
        }
        return null;
    }

    private void saveSessionInfo(List<SeckillSessionsWithSkus> sessions) {
        if(!CollectionUtils.isEmpty(sessions)) {
            sessions.stream().forEach(session->{
                Long startTime = session.getStartTime().getTime();
                Long endTime = session.getEndTime().getTime();
                String key=SESSIONS_CACHE_PREFIX+startTime+"_"+endTime;
                Boolean hasKey = redisTemplate.hasKey(key);
                if(!hasKey) {
                    List<String> collect = session.getRelationSkus().stream()
                            .map(item -> item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString())
                            .collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key,collect);
                }
            });
        }
    }
    private void saveSessionSkuInfo(List<SeckillSessionsWithSkus> sessions) {
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        if(!CollectionUtils.isEmpty(sessions)) {
            sessions.stream().forEach(session->{
                session.getRelationSkus().stream().forEach(seckillSku->{
                    String key=seckillSku.getPromotionSessionId().toString()+"_"+seckillSku.getSkuId().toString();
                    if(!ops.hasKey(key)) {
                        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                        R r = productFeignService.getSkuinfo(seckillSku.getSkuId());
                        if(0==r.getCode()) {
                            SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                            });
                            redisTo.setSkuInfoVo(skuInfo);
                        }
                        //拷贝sku的秒杀信息
                        BeanUtils.copyProperties(seckillSku,redisTo);
                        //设置秒杀时间
                        redisTo.setStartTime(session.getStartTime().getTime());
                        redisTo.setEndTime(session.getEndTime().getTime());
                        //设置随机码
                        String token = UUID.randomUUID().toString().replace("_", "");
                        redisTo.setRandomCode(token);
                        String jsonString = JSON.toJSONString(redisTo);
                        ops.put(key,jsonString);
                        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                        //商品可以秒杀的数量作为信号量
                        semaphore.trySetPermits(seckillSku.getSeckillCount());
                    }
                });
            });
        }
    }
}
