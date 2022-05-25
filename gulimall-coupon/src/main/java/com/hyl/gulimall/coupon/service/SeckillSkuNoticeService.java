package com.hyl.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.coupon.entity.SeckillSkuNoticeEntity;

import java.util.Map;

/**
 * 秒杀商品通知订阅
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:38:28
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

