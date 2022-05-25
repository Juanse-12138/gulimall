package com.hyl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> productAttrValueEntities);

    List<ProductAttrValueEntity> baseAttrlistforspu(Long spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}

