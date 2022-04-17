package com.hyl.gulimall.search.service;

import com.hyl.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/4/11 - 16:52
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
