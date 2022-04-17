package com.hyl.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyl.gulimall.product.service.CategoryBrandRelationService;
import com.hyl.gulimall.product.vo.catelogvo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /*TODO:产生堆外内存溢出*/
    /*springboot2.0后默认使用Lettuce作为操作redis的客户端，使用Netty进行网络通信*/
    /*Lettuce的bug导致netty堆外内存溢出，如果没有堆外内存，默认使用-Xmx300m*/
    /*不能只调大堆外内存，升级Lettuce，切换使用jedis*/
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /*缓存都存JSON，跨平台通用*/
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        Map<String, List<Catelog2Vo>> catalog;
        if(StringUtils.isEmpty(catalogJSON)){
            /*缓存中没有从数据库查*/
            catalog = getCatalogJsonFromDB();
            /*转为JSON保存到缓存*/
            String s = JSON.toJSONString(catalog);
            redisTemplate.opsForValue().set("catalogJSON",s);
        }else{
            catalog = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        }

        return catalog;
    }
    /*从数据库查得二级分类数据*/
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDB() {
        /*多次查库变成一次查库*/
        List<CategoryEntity> categorys = baseMapper.selectList(null);


        /*查询所有的一级分类ID*/
        List<CategoryEntity> level1Categorys = getParent_cid(categorys,0L);

        /*封装数据*/
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            /*根据每个一级分类，查到这个一级分类的二级分类*/
            List<CategoryEntity> categoryEntities = getParent_cid(categorys,v.getCatId());
            /*封装结果*/
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());

                    /*封装三级分类*/
                    List<CategoryEntity> level3Catelogs = getParent_cid(categorys,level2.getCatId());
                    if(level3Catelogs != null){
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = level3Catelogs.stream().map(level3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(catelog2Vo.getId(), level3.getCatId().toString(), level3.getName());

                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());

            }

            return catelog2Vos;
        }));
        return parent_cid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parent_cid) {
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", catId));
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
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
