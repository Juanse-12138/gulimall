package com.hyl.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.hyl.common.constant.ProductConstant;
import com.hyl.common.to.SkuHasStockVo;
import com.hyl.common.to.SkuReductionTo;
import com.hyl.common.to.SpuBoundTo;
import com.hyl.common.to.es.SkuEsModel;
import com.hyl.common.utils.R;
import com.hyl.gulimall.product.entity.*;
import com.hyl.gulimall.product.feign.CouponFeignService;
import com.hyl.gulimall.product.feign.SearchFeignService;
import com.hyl.gulimall.product.feign.WareFeignService;
import com.hyl.gulimall.product.service.*;
import com.hyl.gulimall.product.vo.spuvo.*;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /*TODO:??????????????????*/
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        /*1.??????spu???????????? `pms_spu_info`*/
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        /*2.??????spu??????????????? `pms_spu_info_desc`*/
        List<String> spuDecript = spuSaveVo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", spuDecript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        /*3.??????spu???????????? `pms_spu_images`*/
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        /*4.??????spu??????????????? `pms_product_attr_value`*/
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());

            /*??????attr???vo??????baseattr*/
            AttrEntity id = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(id.getAttrName());

            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueEntities);

        /*6.??????spu???????????????(??????????????? */
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r1 = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r1.getCode() != 0){
            log.error("????????????spu??????????????????");
        }

        /*5.????????????spu??????????????????sku??????*/
        List<Skus> skus = spuSaveVo.getSkus();
        if(skus != null && !skus.isEmpty()){
            skus.forEach(item->{
                    /*????????????*/
                String defaultImage = "";
                for(Images image : item.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImage = image.getImgUrl();
                    }
                }
                /*5.1 sku??????????????? `pms_sku_info`*/
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                /*??????skus??????????????????????????????*/
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                /*5.2 sku??????????????? `pms_sku_images`*/
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();

                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    /*??????true???????????????false??????*/
                    return StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                /*5.3 sku??????????????? `pms_sku_sale_attr_value`*/
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                /*5.4 sku????????????????????????(???????????????*/
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){

                    R r2 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r2.getCode() != 0){
                        log.error("????????????sku??????????????????");
                    }
                }

            });
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            /*????????????????????????????????????or????????????????????????and????????????*/
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        /*??????????????????Long*/
        String catelogId = (String) params.get("catelogId");
        /*???????????????????????????0??????null???undifined?????????*/
        if(!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        IPage<SpuInfoEntity> page = this.page(
        new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        //?????????????????????
        List<SkuEsModel> upProducts=new ArrayList<>();
        //1???????????????spuId???????????????sku?????????????????????
        List<SkuInfoEntity> skus=skuInfoService.getSkusBySpuId(spuId);
        //TOdo 3???????????????sku??????????????????????????????????????????
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrIds=attrService.selectSearchAttrs(attrIds);
        Set<Long> idSet=new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //TODO 1??????????????????????????????????????????
        Map<Long, Boolean> stockMap=null;
        try{
            R skusHasStock = wareFeignService.getSkusHasStock(skuIds);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            stockMap = skusHasStock.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e) {
            log.error("?????????????????????????????????{}",e);
        }
       //2???????????????sku??????
        Map<Long, Boolean> finalStockMap = stockMap;
        upProducts=skus.stream().map(sku->{
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,esModel);
            //?????????????????????????????????????????????
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            //??????????????????
            if(finalStockMap ==null) {
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //TODO 2???????????????  ???????????????0
            esModel.setHotScore(0L);
            //????????????????????????????????????
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg((brand.getLogo()));

            CategoryEntity category= categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());
            //??????????????????
            esModel.setAttrs(attrsList);
            return esModel;
        }).collect(Collectors.toList());
        //TODO ????????????????????????es???????????????gulimall-search

        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode()==0) {
            //??????????????????
            //TODO 6 ????????????spu??????
            baseMapper.updateStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            //??????????????????
            //TODO 7????????????????????????????????????????????????
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        Long spuId = byId.getSpuId();
        SpuInfoEntity spuInfoEntity = getById(spuId);

        return spuInfoEntity;
    }
}