package com.hyl.gulimall.seckill.feign;

import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @GetMapping("coupon/seckillsession/lastest3DaysSession")
    public R getLastest3DaysSession();
}
