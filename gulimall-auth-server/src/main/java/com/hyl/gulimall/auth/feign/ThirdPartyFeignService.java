package com.hyl.gulimall.auth.feign;

import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hyl_marco
 * @data 2022/5/9 - 17:47
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code")String code);
}
