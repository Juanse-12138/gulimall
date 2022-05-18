package com.hyl.gulimall.product.vo.skuItemvo;

import lombok.Data;
import lombok.ToString;

import java.util.List;


/**
 * 这个VO最好写成skuitemvo的内部类
 * xie
 */
@Data
@ToString
public class SkuItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
