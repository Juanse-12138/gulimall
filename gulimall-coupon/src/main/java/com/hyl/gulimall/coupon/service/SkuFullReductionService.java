package com.hyl.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.to.SkuReductionTo;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:38:28
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

