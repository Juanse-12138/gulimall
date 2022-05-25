package com.hyl.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.hyl.common.utils.R;
import com.hyl.gulimall.product.entity.SkuImagesEntity;
import com.hyl.gulimall.product.entity.SpuInfoDescEntity;
import com.hyl.gulimall.product.feign.SeckillFeignService;
import com.hyl.gulimall.product.service.*;
import com.hyl.gulimall.product.vo.skuItemvo.SkuItemSaleAttrsVo;
import com.hyl.gulimall.product.vo.skuItemvo.SkuItemVo;
import com.hyl.gulimall.product.vo.skuItemvo.SpuItemAttrGroupVo;
import com.hyl.gulimall.product.vo.spvsavevo.SeckillInfoVo;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.asn1.esf.CompleteRevocationRefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.product.dao.SkuInfoDao;
import com.hyl.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        /*page: 1
          limit: 10
          key:
          catelogId: 0
          brandId: 0
          min: 0
          max: 0*/
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            /*这里用箭头函数是为了将用or连接的两个条件在and里括起来*/
            wrapper.and((w)->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String max = (String) params.get("max");
        if(!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal("0")) == 1){

                    wrapper.le("price", max);
                }
            }catch (Exception e){

            }
        }
        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)) {
            wrapper.ge("price", min);
        }



        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        List<SkuInfoEntity> list=this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id",spuId));
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1、sku基本信息
            SkuInfoEntity sku = getById(skuId);
            skuItemVo.setInfo(sku);
            return sku;
        }, executor);

        CompletableFuture<Void> saleFuture = infoFuture.thenAcceptAsync((res) -> {
            //3、获取spu销售属性集合
            List<SkuItemSaleAttrsVo> saleAttrsVos = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrsVos);
        }, executor);

        CompletableFuture<Void> despFuture = infoFuture.thenAcceptAsync((res) -> {
            //4、spu介绍
            SpuInfoDescEntity spuDesc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesp(spuDesc);
        }, executor);

        CompletableFuture<Void> attrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5、spu规格参数信息
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);

        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImage(skuId);
            skuItemVo.setImagesEntites(images);
        }, executor);
//        Long catalogId=sku.getCatalogId();
//        Long spuId=sku.getSpuId();
        //2、图片信息
        //等待所有异步任务都完成后，返回vo
        //获取商品详情的秒杀信息
        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSecKillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillInfoVo vo = r.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfo(vo);
            }
        }, executor);

        CompletableFuture.allOf(saleFuture,attrFuture,imageFuture,despFuture,seckillFuture).get();
        return skuItemVo;

    }

}