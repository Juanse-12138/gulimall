package com.hyl.gulimall.product.feign;

import com.hyl.common.to.es.SkuEsModel;
import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/4/11 - 17:35
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUP(@RequestBody List<SkuEsModel> skuEsModels);
}
