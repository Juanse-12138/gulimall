package com.hyl.gulimall.product.vo.skuItemvo;

import com.hyl.gulimall.product.vo.spuvo.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrValues;
}
