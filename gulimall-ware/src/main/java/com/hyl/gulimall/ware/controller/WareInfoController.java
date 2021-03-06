package com.hyl.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Map;

import com.hyl.gulimall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hyl.gulimall.ware.entity.WareInfoEntity;
import com.hyl.gulimall.ware.service.WareInfoService;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.R;

import javax.annotation.Resource;


/**
 * 仓库信息
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 19:17:39
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WareInfoController {

    @Autowired
    private WareInfoService wareInfoService;

    @GetMapping("/")
    public String test(){
        return "test success!";
    }

    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId){
        System.out.println("查询"+addrId+"的运费");
        FareVo fare = wareInfoService.getFare(addrId);
        return R.ok().setData(fare);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareInfoEntity wareInfo = wareInfoService.getById(id);

        return R.ok().put("wareInfo", wareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.save(wareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareInfoEntity wareInfo){
		wareInfoService.updateById(wareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
