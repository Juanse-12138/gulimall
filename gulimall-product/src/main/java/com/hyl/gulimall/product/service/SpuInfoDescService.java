package com.hyl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity descEntity);
}

