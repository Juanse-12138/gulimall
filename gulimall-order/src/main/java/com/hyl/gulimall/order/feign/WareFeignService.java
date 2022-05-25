package com.hyl.gulimall.order.feign;

import com.hyl.common.utils.R;
import com.hyl.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-ware")
public interface WareFeignService {
    @GetMapping("ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("ware/waresku/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo);
}
