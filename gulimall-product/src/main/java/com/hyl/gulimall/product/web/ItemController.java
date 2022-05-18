package com.hyl.gulimall.product.web;

import com.hyl.gulimall.product.service.SkuInfoService;
import com.hyl.gulimall.product.vo.skuItemvo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @author hyl_marco
 * @data 2022/5/1 - 16:07
 */
@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    /**
     *展示当前Sku的详情
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuName(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {

        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item",skuItemVo);
        return "item";
    }
}
