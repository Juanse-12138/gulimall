package com.hyl.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.hyl.gulimall.product.entity.AttrEntity;
import com.hyl.gulimall.product.service.AttrAttrgroupRelationService;
import com.hyl.gulimall.product.service.AttrService;
import com.hyl.gulimall.product.service.CategoryService;
import com.hyl.gulimall.product.vo.AttrGroupRelationVo;
import com.hyl.gulimall.product.vo.AttrGroupwithAttrsRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hyl.gulimall.product.entity.AttrGroupEntity;
import com.hyl.gulimall.product.service.AttrGroupService;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.R;



/**
 * 属性分组
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /*获取当前属性分类下的所有属性分组以及该分组关联的属性*/
    /*/product/attrgroup/{catelogId}/withattr*/
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        /*查出属性分组*/
        /*查出分组的所有属性*/
        List<AttrGroupwithAttrsRespVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);
    }

    /*/product/attrgroup/{attrgroupId}/attr/relation*/
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> list =  attrService.getAttrRelationAttr(attrgroupId);

        return R.ok().put("data",list);
    }

    /*/product/attrgroup/{attrgroupId}/noattr/relation*/
    /*注意这里要获得分页数据*/
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params
    ){
        PageUtils page = attrService.getNoRelationAttr(params,attrgroupId);

        return R.ok().put("page",page);
    }

    /*/product/attrgroup/attr/relation*/
    @PostMapping("/attr/relation")
    public R attrRelationCreate(@RequestBody List<AttrGroupRelationVo> vos ){
            relationService.saveBatch(vos);

        return R.ok();
    }


    /*/product/attrgroup/attr/relation/delete*/
    /*这里注意@RequestBody的用法,将需求传来的参数封装到指定实体类,即将json封装到实体类数组*/
    @PostMapping("/attr/relation/delete")
    public R attrRelationDelete(@RequestBody AttrGroupRelationVo[] vos){
        attrService.deleteRelation(vos);
        return R.ok();

    }
    /**
     * 列表
     * 注意这里是一个分页方法，最终返回pageutils数据
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
