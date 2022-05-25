package com.hyl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> getImage(Long skuId);
}

