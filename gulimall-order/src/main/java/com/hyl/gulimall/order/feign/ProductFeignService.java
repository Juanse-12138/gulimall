package com.hyl.gulimall.order.feign;

import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("product/spuinfo/skuId/{id}")
    public R getSpuInfoBySkuId(@PathVariable("id") Long skuId);
}
