package com.hyl.gulimall.ware.feign;

import com.hyl.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hyl_marco
 * @data 2022/3/26 - 17:54
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /*
     * product/skuinfo/info/{skuId}
     *
     * api/product/skuinfo/info/{skuId}
     *  请求给网关，feignclient中也写网关服务
     * */
    @RequestMapping("product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

}
