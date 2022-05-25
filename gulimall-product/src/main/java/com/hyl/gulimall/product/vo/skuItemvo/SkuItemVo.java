package com.hyl.gulimall.product.vo.skuItemvo;

import com.hyl.gulimall.product.entity.SkuImagesEntity;
import com.hyl.gulimall.product.entity.SkuInfoEntity;
import com.hyl.gulimall.product.entity.SpuInfoDescEntity;
import com.hyl.gulimall.product.vo.spvsavevo.SeckillInfoVo;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemVo {
    //1.sku基本信息获取 pms_sku_info
    SkuInfoEntity info;

    boolean hasStock = true;
    //2.sku图片信息 pms_sku_images
    List<SkuImagesEntity> imagesEntites;

    //3.spu的销售属性组合
    List<SkuItemSaleAttrsVo> saleAttr;

    //4.spu的详细介绍
    SpuInfoDescEntity desp;

    //5.规格参数
    List<SpuItemAttrGroupVo> groupAttrs;

    //6.当前商品的秒杀优惠信息
    SeckillInfoVo seckillInfo;

}
