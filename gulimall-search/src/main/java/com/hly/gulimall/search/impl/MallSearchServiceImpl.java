package com.hly.gulimall.search.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hly.gulimall.search.config.GulimallElasticSearchConfig;
import com.hly.gulimall.search.constant.EsConstant;
import com.hly.gulimall.search.feign.ProductFeignService;
import com.hly.gulimall.search.service.MallSearchService;
import com.hly.gulimall.search.vo.AttrResponseVo;
import com.hly.gulimall.search.vo.BrandVo;
import com.hly.gulimall.search.vo.SearchParam;
import com.hly.gulimall.search.vo.SearchResult;
import com.hyl.common.to.es.SkuEsModel;
import com.hyl.common.utils.R;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        SearchResult result=null;
        //准备检索请求
        SearchRequest searchRequest=buildSearchRequest(param);
        SearchResponse response= null;
        try {
            //执行检索请求
            response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            result=buildSearchResult(response,param);
            //封装相应数据为指定格式
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {

        SearchResult result = new SearchResult();
        //1、返回所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if(!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
       }
        result.setProducts(esModels);
// //2、当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
//1、得到属性的 id
            long attrId = bucket.getKeyAsNumber().longValue();
//2、得到属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
//3、得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms)
                    bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
// //3、当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
//1、得到品牌的 id
            long brandId = bucket.getKeyAsNumber().longValue();
//2、得到品牌的名
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
//3、得到品牌的图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);
// //4、当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
//得到分类 id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
//得到分类名
            ParsedStringTerms catalog_name_agg =
                    bucket.getAggregations().get("catalog_name_agg");
            String catalog_name =
                    catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);
// ========以上从聚合信息中获取======
// //5、分页信息-页码
        result.setPageNum(param.getPageNum());
// //5、分页信息-总记录树
        long total = hits.getTotalHits().value;
        result.setTotal(total);
// //5、分页信息-总页码-计算 11/2 = 5 .. 1
        long totalPage = total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPage(totalPage);
        List<Integer> pageNavs = new ArrayList<>();
        for (int i=1;i<=totalPage;i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
//6、构建面包屑导航功能
        if(param.getAttrs()!=null && param.getAttrs().size()>0){
            List<SearchResult.NavVo> collect =
                    param.getAttrs().stream().map(attr -> {
//1、分析每个 attrs 传过来的查询参数值。
                        SearchResult.NavVo navVo = new SearchResult.NavVo();
// attrs=2_5 存:6 寸
                        String[] s = attr.split("_");
                        navVo.setNavValue(s[1]);
                        R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                        result.getAttrIds().add(Long.parseLong(s[0]));
                        if(r.getCode() == 0){
                            AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>(){});
                            navVo.setNavName( data.getAttrName());
                        }else{
                            navVo.setNavName(s[0]);
                        }
//
//2、取消了这个面包屑以后，我们要跳转到那个地方.将请求地址的 url里面的当前置空
//拿到所有的查询条件，去掉当前。
//attrs= 15_海思（Hisilicon）
                        String replace = replaceQueryString(param, attr,"attrs");
                        navVo.setLink("http://search.gulimall.com/list.html?"+replace);
                        return navVo;
                    }).collect(Collectors.toList());
            result.setNavs(collect);
        }
//品牌，分类
        if(param.getBrandId()!=null && param.getBrandId().size()>0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
//TODO 远程查询所有品牌
            R r = productFeignService.brandsInfo(param.getBrandId());
            if(r.getCode() == 0){
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName()+";");
                    replace = replaceQueryString(param, brandVo.getBrandId()+"","brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?"+replace);
            }
            navs.add(navVo);
        }
//TODO 分类：不需要导航取消
        return result;
    }
    private String replaceQueryString(SearchParam param, String value,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+","%20");//浏览器对空格编码和 java 不一样
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&"+key+"=" + encode, "");
    }

    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1、构建 bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//1.1、must-模糊匹配，
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
//1.2、bool - filter - 按照三级分类 id 查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
//1.2、bool - filter - 按照品牌 id 查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
//1.2、bool - filter - 按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
//attrs=1_5 寸:8 寸&attrs=2_16G:8G
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
//attr = 1_5 寸:8 寸
                String[] s = attrStr.split("_");
                String attrId = s[0]; //检索的属性 id
                String[] attrValues = s[1].split(":"); //这个属性的检索用的值
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
//每一个必须都得生成一个 nested 查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
//1.2、bool - filter - 按照库存是否有进行查询
        if(param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termQuery("hasStock",
                    param.getHasStock() == 1));
        }
//1.2、bool - filter - 按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //1_500/_500/500_
            /**
             * "range": {
             * "skuPrice": {
             * "gte": 0,
             * "lte": 6000
             * }
             * }
             */
            RangeQueryBuilder rangeQuery =
                    QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
            //区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
//把以前的所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);
/**
 * 排序，分页，高亮，
 */
//2.1、排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
//sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
//2.2、分页 pageSize:5
// pageNum:1 from:0 size:5 [0,1,2,3,4]
// pageNum:2 from:5 size:5
//from = (pageNum-1)*size
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
//2.3、高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
/**
 * 聚合分析
 */
//1、品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
//品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
//TODO 1、聚合 brand
        sourceBuilder.aggregation(brand_agg);
//2、分类聚合 catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
//TODO 2、聚合 catalog
        sourceBuilder.aggregation(catalog_agg);
//3、属性聚合 attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
//聚合出当前所有的 attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
//聚合分析出当前 attr_id 对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
//聚合分析出当前 attr_id 对应的所有可能的属性值 attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
//TODO 3、聚合 attr
        sourceBuilder.aggregation(attr_agg);
        String s = sourceBuilder.toString();
        System.out.println("构建的 DSL" + s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}
