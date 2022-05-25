package com.hyl.gulimall.product.service.impl;

import com.hyl.gulimall.product.service.CategoryBrandRelationService;
import com.hyl.gulimall.product.vo.BrandVo;
import org.apache.commons.lang.StringUtils;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.product.dao.BrandDao;
import com.hyl.gulimall.product.entity.BrandEntity;
import com.hyl.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("brand_id",key).or().like("name",key);
        }


        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        /*保证冗余字段的数据一致*/
        this.updateById(brand);
        if(!StringUtils.isEmpty(brand.getName())){
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

            /*TODO更新其他关联*/
        }
    }

    @Override
    public List<BrandVo> getBrandByIds(List<Long> brandIds) {

        List<BrandEntity> brandEntities = baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id",brandIds));
        List<BrandVo> brandVos = new ArrayList<>();
        for(BrandEntity entity:brandEntities) {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(entity.getBrandId());
            brandVo.setBrandName(entity.getName());
            brandVos.add(brandVo);
        }

        return brandVos;
    }

}