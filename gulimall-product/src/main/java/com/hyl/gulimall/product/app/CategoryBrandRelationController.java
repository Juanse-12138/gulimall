package com.hyl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hyl.gulimall.product.entity.BrandEntity;
import com.hyl.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hyl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.hyl.gulimall.product.service.CategoryBrandRelationService;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 *
 * Controller：处理请求，接受和校验数据
 *      Service接收controller传来的数据，进行业务处理
 *      Service通过自身的方法、自身dao的接口，结合需要的其他service接口完成业务处理
 *          dao进行持久化操作
 * Controller接受Service处理完的数据，封装页面指定的vo、page等
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /*/product/categorybrandrelation/brands/list*/
    /*required=true等于必须传递的参数*/
    @GetMapping(value = "/brands/list")
    public R categoryBrandRelation(@RequestParam(value = "catId",required = true) Long catId) {
        List<BrandEntity> data = categoryBrandRelationService.getBrandsByCatId(catId);
        /*这个逻辑是否应该写在Service层*/
        List<BrandVo> collect = data.stream().map(item -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(item.getBrandId());
            brandVo.setBrandName(item.getName());
            return brandVo;
        }).collect(Collectors.toList());

        return R.ok().put("data", collect);
    }

    /**
     * 获取品牌当前关联的所有分类列表
     */
//    @RequestMapping(value = "/catelog/list",method = RequestMethod.GET)
    @GetMapping(value = "/catelog/list")
    public R catloglist(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId)
                );

        return R.ok().put("data", data);
    }



    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
