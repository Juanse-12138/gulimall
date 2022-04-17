package com.hyl.gulimall.product.feign;

import com.hyl.common.to.SkuReductionTo;
import com.hyl.common.to.SpuBoundTo;
import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author hyl_marco
 * @data 2022/3/24 - 15:06
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /*
    *
    * ！！！只要json数据模型是兼容的，双方服务无需使用同一个TO
    *
    * 本质上是给其他服务以json的格式传输数据，只要两方实体对象中字段变量有重合即可
    * */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
