package com.hyl.gulimall.order.feign;

import com.hyl.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    public List<OrderItemVo> getCurrentUserCartItems();
}
