package com.hyl.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyl.gulimall.product.service.CategoryBrandRelationService;
import com.hyl.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hyl.common.utils.PageUtils;
import com.hyl.common.utils.Query;

import com.hyl.gulimall.product.dao.CategoryDao;
import com.hyl.gulimall.product.entity.CategoryEntity;
import com.hyl.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        /*查出所有分类*/
        List<CategoryEntity> entities = baseMapper.selectList(null);

        /*组装树形结构*/
            /*找到一级分类*/
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
            categoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildrens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            int sort1 = menu1.getSort() == null ? 0 : menu1.getSort();
            int sort2 = menu2.getSort() == null ? 0 : menu2.getSort();
            return sort1 - sort2;
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {

        /*TODO: 检查当前删除的菜单，是否被其他地方引用*/

        /*逻辑删除*/
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId,paths);

        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)) {
            //缓存中没有该数据，需要取db获取
            Map<String, List<Catelog2Vo>> catelogJsonFromDB = getCatelogJsonFromDB();
            //从数据库查出来的数据转为json放在缓存中
            redisTemplate.opsForValue().set("catalogJSON",JSON.toJSONString(catelogJsonFromDB));
        }
        Map<String, List<Catelog2Vo>> result=JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    //从数据库查询并封装分类数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDB() {

        List<CategoryEntity> level1Categorys = getLevel1Categorys();
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //遍历每个一级分类，查询其所有二级分类
            List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());
                    //找三级分类

                    List<CategoryEntity> catelog3 = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", item.getCatId()));
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos=null;
                    if(catelog3!=null) {
                        catelog3Vos = catelog3.stream().map(cat -> new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), cat.getCatId().toString(), cat.getName())
                        ).collect(Collectors.toList());
                    }
                    catelog2Vo.setCatalog3List(catelog3Vos);
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    private List<Long> findParentPath(Long catelogId,List<Long> paths){
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if(byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    /*递归查找每个菜单的子菜单*/
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map((menu) -> {
            menu.setChildren(getChildrens(menu, all));
            return menu;
        }).sorted((menu1, menu2) -> {
            int sort1 = menu1.getSort() == null ? 0 : menu1.getSort();
            int sort2 = menu2.getSort() == null ? 0 : menu2.getSort();
            return sort1 - sort2;
        }).collect(Collectors.toList());
        return children;
    }

}