package com.hyl.gulimall.product.vo;

import com.hyl.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/3/23 - 13:10
 */
@Data
public class AttrGroupwithAttrsRespVo {

    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /*该属性分组下的所有属性*/
    private List<AttrEntity> attrs;
}
