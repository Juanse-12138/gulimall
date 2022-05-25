package com.hyl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hyl.gulimall.product.entity.ProductAttrValueEntity;
import com.hyl.gulimall.product.service.ProductAttrValueService;
import com.hyl.gulimall.product.vo.AttrRespVo;
import com.hyl.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hyl.gulimall.product.service.AttrService;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.R;



/**
 * 商品属性
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;



    /*/product/attr/base/listforspu/{spuId}*/
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrlistforspu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> entities = productAttrValueService.baseAttrlistforspu(spuId);
        return R.ok().put("data",entities);
    }


    /*/product/attr/sale/list/{catelogId}*/
    /*/product/attr/base/list/{catelogId}*/
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String,Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType
    ){

        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,attrType);
        return R.ok().put("page",page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    /*/product/attr/info/{attrId}*/
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
        /*舍弃原生的*/
//		AttrEntity attr = attrService.getById(attrId);

		/*要获得属性的 所属类别的路径信息 分组信息*/
        AttrRespVo attrRespVo =  attrService.getAttrInfo(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
//		attrService.updateById(attr);

        attrService.updateAttr(attr);
        return R.ok();
    }

    /*/product/attr/update/{spuId}*/
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities) {
        productAttrValueService.updateSpuAttr(spuId,entities);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
