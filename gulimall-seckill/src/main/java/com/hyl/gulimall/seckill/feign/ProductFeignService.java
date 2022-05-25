package com.hyl.gulimall.seckill.feign;

import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("product/skuinfo/info/{skuId}")
    public R getSkuinfo(@PathVariable("skuId") Long skuId);
}
