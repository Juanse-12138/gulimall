package com.hyl.gulimall.product.service.impl;

import com.hyl.gulimall.product.vo.skuItemvo.SkuItemSaleAttrsVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.product.dao.SkuSaleAttrValueDao;
import com.hyl.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.hyl.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrsVo> getSaleAttrsBySpuId(Long spuId) {

        SkuSaleAttrValueDao baseMapper = this.baseMapper;
        List<SkuItemSaleAttrsVo> list=baseMapper.getSaleAttrsBySpuId(spuId);
        return list;
    }

    @Override
    public List<String> getSaleAttrStringList(Long skuId) {
        List<String> list=baseMapper.getSaleAttrStringList(skuId);
        return list;
    }

}