package com.hyl.gulimall.search.vo;

import com.hyl.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {

    /*查询到的所有商品信息*/
    private List<SkuEsModel> products;

    /**
     * 分页信息
     */
    private Integer pageNum;//当前页
    private Long total;//总记录数
    private Long totalPage;//总页码
    private List<Integer> pageNavs;//用于页码遍历

    private List<BrandVo> brands;//品牌信息，当前查询到商品所涉及到的所有品牌
    private List<AttrVo> attrs;//属性信息
    private List<CatalogVo> catalogs;//分类信息
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    /*===========以上是返回给页面的所有信息===========*/

    /**
     *面包屑导航静态内部类
     */
    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }
    /**
     * 品牌内部类，封装检索出来所有商品对应的品牌
     */
    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }
    /**
     * 属性内部类
     */
    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
    /**
     * 分类内部类
     */
    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }
}

