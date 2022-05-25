package com.hyl.gulimall.order.feign;

import com.hyl.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @GetMapping("member/memberreceiveaddress/{memberId}/addresses")
    public List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
