package com.hyl.gulimall.ware.feign;

import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("order/order/status/{orderSn}")
    public R infoByOrderSn(@PathVariable("orderSn") String orderSn);
}
