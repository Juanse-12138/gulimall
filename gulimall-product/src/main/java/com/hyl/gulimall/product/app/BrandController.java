package com.hyl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hyl.common.validator.group.AddGroup;
import com.hyl.common.validator.group.UpdateGroup;
import com.hyl.common.validator.group.UpdateGroupStatus;
import com.hyl.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hyl.gulimall.product.entity.BrandEntity;
import com.hyl.gulimall.product.service.BrandService;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.R;


/**
 * 品牌
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandVo> brandVos= brandService.getBrandByIds(brandIds);
        return R.ok().put("brand",brandVos);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand){
//        if(result.hasErrors()){
//            /*获取错误结果*/
//            Map<String,String> map = new HashMap<>();
//
//            result.getFieldErrors().forEach((item) -> {
//                String message = item.getDefaultMessage();
//                String field = item.getField();
//                map.put(field,message);
//            });
//
//           return R.error(400,"数据不合法").put("data",map);
//        }else{
//        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand){
        /*不只要更新品牌表，关联表中的冗余数据也需要修改*/
//        brandService.updateById(brand);
        brandService.updateDetail(brand);
        return R.ok();
    }

    @RequestMapping("/update/status")
    public R updateStatus(@Validated({UpdateGroupStatus.class}) @RequestBody BrandEntity brand){

        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
