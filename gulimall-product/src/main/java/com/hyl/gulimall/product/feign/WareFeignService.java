package com.hyl.gulimall.product.feign;

import com.hyl.common.to.SkuHasStockVo;
import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author hyl_marco
 * @data 2022/4/10 - 18:00
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    /*查询sku是否有库存*/
    /*R的泛型data字段*/
    /*改Controller的返回*/
    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
