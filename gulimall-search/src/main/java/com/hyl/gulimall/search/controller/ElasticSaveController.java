package com.hyl.gulimall.search.controller;

import com.hyl.common.exception.BizCodeEnume;
import com.hyl.common.to.es.SkuEsModel;
import com.hyl.common.utils.R;
import com.hyl.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/4/10 - 19:02
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    /*上架商品*/
    @PostMapping("/product")
    public R productStatusUP(@RequestBody List<SkuEsModel> skuEsModels){
        boolean b = false;
        try {
            /*这里b为true是有错误的意思？是的*/
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (Exception e) {
            log.error("ElasticSaveController商品上架错误：{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        /*这里改成!b*/
        if(!b){
            return R.ok();
        }else{
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
