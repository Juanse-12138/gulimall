package com.hyl.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hyl.gulimall.product.dao.BrandDao;
import com.hyl.gulimall.product.dao.CategoryDao;
import com.hyl.gulimall.product.entity.BrandEntity;
import com.hyl.gulimall.product.entity.CategoryEntity;
import com.hyl.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.product.dao.CategoryBrandRelationDao;
import com.hyl.gulimall.product.entity.CategoryBrandRelationEntity;
import com.hyl.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Resource
    BrandDao brandDao;
    @Resource
    CategoryDao categoryDao;

    @Resource
    CategoryBrandRelationDao categoryBrandRelationDao;

    /*要调用别的最好调用Service，业务实现更加的丰富*/
    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);

    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandName(name);
        categoryBrandRelationEntity.setBrandId(brandId);
        this.update(categoryBrandRelationEntity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId,name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        /*该方法可能回被其他controller调用，因此还是返回Brand的实体类，而不是返回中间表实体类*/
        List<CategoryBrandRelationEntity> relationEntities = categoryBrandRelationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId)
        );
        List<BrandEntity> collect = relationEntities.stream().map(item -> {
            Long brandId = item.getBrandId();
            /*这里不建议进行循环查询*/
            BrandEntity brandEntity = brandService.getById(brandId);
            return brandEntity;
        }).collect(Collectors.toList());
        return collect;
    }

}