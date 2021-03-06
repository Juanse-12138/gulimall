package com.hyl.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hyl.common.exception.BizCodeEnume;
import com.hyl.common.exception.NoStockException;
import com.hyl.gulimall.ware.vo.SkuHasStockVo;
import com.hyl.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hyl.gulimall.ware.entity.WareSkuEntity;
import com.hyl.gulimall.ware.service.WareSkuService;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.R;



/**
 * 商品库存
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 19:17:39
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    //查询库存sku是否有库存
    @PostMapping("/hasStock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds) {

        List<SkuHasStockVo> vos= wareSkuService.getSkusHasStock(skuIds);
        return R.ok().setData((vos));
    }

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareSkuLockVo vo){
        System.out.println("执行锁库存方法");
        try{
            Boolean stock = wareSkuService.orderLockStock(vo);
            return R.ok();
        }catch (NoStockException e){
            return R.error(BizCodeEnume.NO_STOCK_EXCEPTION.getCode(), BizCodeEnume.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
