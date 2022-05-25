package com.hly.gulimall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword; //全文匹配关键字
    private Long catalog3Id;
    private String sort;
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId; //按照品牌进行查询，可以多选
    private List<String> attrs; //按照属性进行筛选，可以多选
    private Integer pageNum=1;  //页码

    private String _queryString; //原生查询条件
}
