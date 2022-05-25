package com.hyl.gulimall.product.feign;

import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping(value = "/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
