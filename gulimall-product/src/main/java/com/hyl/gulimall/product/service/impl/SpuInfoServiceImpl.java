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

    /*TODO:高级部分改善*/
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        /*1.保存spu基本信息 `pms_spu_info`*/
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        /*2.保存spu的描述图片 `pms_spu_info_desc`*/
        List<String> spuDecript = spuSaveVo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", spuDecript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        /*3.保存spu的图片集 `pms_spu_images`*/
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(),images);

        /*4.保存spu的规格参数 `pms_product_attr_value`*/
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());

            /*这里attr是vo里的baseattr*/
            AttrEntity id = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(id.getAttrName());

            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueEntities);

        /*6.保存spu的积分信息(跨服务器） */
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r1 = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r1.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        /*5.保存当前spu中对应的所有sku信息*/
        List<Skus> skus = spuSaveVo.getSkus();
        if(skus != null && !skus.isEmpty()){
            skus.forEach(item->{
                    /*默认图片*/
                String defaultImage = "";
                for(Images image : item.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImage = image.getImgUrl();
                    }
                }
                /*5.1 sku的基本信息 `pms_sku_info`*/
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item,skuInfoEntity);
                /*设置skus中没有的基本信息字段*/
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                /*5.2 sku的图片信息 `pms_sku_images`*/
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();

                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity->{
                    /*返回true保留，返回false剔除*/
                    return StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                /*5.3 sku的销售属性 `pms_sku_sale_attr_value`*/
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                /*5.4 sku的优惠、满减信息(跨服务器）*/
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){

                    R r2 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r2.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
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
            /*这里用箭头函数是为了将用or连接的两个条件在and里括起来*/
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        /*这里能不能用Long*/
        String catelogId = (String) params.get("catelogId");
        /*这里在前端将默认的0改为null（undifined）也行*/
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

        /*1.查出当前spuid对象的所有sku*/
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        /*todo:查询当前sku所有可以用来检索的规格属性*/
        /*查询与当前spu关联的所有属性（商品-属性关联表里）*/
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        /*查询出这些属性中能够被检索的（属性表里）*/
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);

        /*将能够被检索的属性封装为SkuEsModel.Attrs实体对象集合，在后面封装到每个Sku的SkuEsModel实体对象*/
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsModelList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrsModel = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrsModel);
            return attrsModel;
        }).collect(Collectors.toList());

        /*TODO:是否有库存,远程调用*/
        Map<Long, Boolean> hasStockMap = null;
        /*远程调用可能抛异常*/
        try{
            /*这里的Lambda表达式需要理解一下*/
            List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            R skusHasStock = wareFeignService.getSkusHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            hasStockMap = skusHasStock.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e){
            log.error("库存服务查询异常：原因{}",e);
        }


        /*2.封装每个sku的信息*/
        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            /*SkuInfoEntity和SkuEsModel对应不上的数据*/
            /*skuPrice,skuImg,hasStock,hotScore */
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            /*hasStock,hotScore*/
            /*设置库存信息*/
            /*Variable used in lambda expression should be final or effective fianl!*/
            esModel.setHasStock(finalHasStockMap == null ? true : finalHasStockMap.get(sku.getSkuId()));

            /*TODO:热度评分*/
            esModel.setHotScore(0L);
            /*brandName,brandImg,cataLogName;
             * */
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandImg(brandEntity.getLogo());
            esModel.setBrandName(brandEntity.getName());
            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());

            /*List<Attrs> attrs设置检索属性**/
            esModel.setAttrs(attrsModelList);
            return esModel;
        }).collect(Collectors.toList());


        /*TODO：保存至es*/
        R r = searchFeignService.productStatusUP(upProducts);
        if(r.getCode() == 0){
            /*TODO：远程调用上架成功，修改当前spu状态*/
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP_SPU.getCode());
        }else{
            /*TODO：远程调用上架失败，重复调用，接口幂等性，重试机制*/
        }
    }


}
