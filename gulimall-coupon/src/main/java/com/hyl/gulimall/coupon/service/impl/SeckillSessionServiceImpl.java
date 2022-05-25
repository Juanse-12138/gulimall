package com.hyl.gulimall.coupon.service.impl;

import com.hyl.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.hyl.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.coupon.dao.SeckillSessionDao;
import com.hyl.gulimall.coupon.entity.SeckillSessionEntity;
import com.hyl.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLastest3DaysSession() {
//        LocalDate now = LocalDate.now();
//        LocalDate end = now.plus(Duration.ofDays(3));
        String s1=startTime();
        String s2=endTime();
        List<SeckillSessionEntity> list = baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        //对活动关联的商品进行封装
        if(list!=null&&list.size()>0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                List<SeckillSkuRelationEntity> skus = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
                session.setRelationSkus(skus);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }
    private String startTime() {
        LocalDate now=LocalDate.now();
        LocalTime time = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, time);

        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }
    private String endTime() {
        LocalDate now=LocalDate.now();
        LocalDate end = now.plusDays(2);
        LocalTime time = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(end, time);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}