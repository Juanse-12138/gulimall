package com.hyl.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hyl.gulimall.product.service.CategoryBrandRelationService;
import com.hyl.gulimall.product.vo.catelogvo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private RedissonClient redissonClient;

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

    /*这里在更新category的时候采用失效模式，删除之前的缓存*/
    /*@CacheEvict失效模式，@CachePut双写模式*/
//    @Caching(evict = {
//            @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category",key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    //当前结果需要缓存，如果有不用调用查缓存
    //并且设置缓存的分区，建议根据业务类型分,可以放多个
    //key是一个表达式，字符串注意加上单引号
    @Cacheable(value = {"category"},key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys被调用。");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /*结合spring-cache再次重写一下这个方法*/
    @Cacheable(value = {"category"},key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson(){
        /*多次查库变成一次查库*/
        List<CategoryEntity> categorys = baseMapper.selectList(null);
        /*查询所有的一级分类ID*/
        List<CategoryEntity> level1Categorys = getParent_cid(categorys, 0L);

        /*封装数据*/
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            /*根据每个一级分类，查到这个一级分类的二级分类*/
            List<CategoryEntity> categoryEntities = getParent_cid(categorys, v.getCatId());
            /*封装结果*/
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());

                    /*封装三级分类*/
                    List<CategoryEntity> level3Catelogs = getParent_cid(categorys, level2.getCatId());
                    if (level3Catelogs != null) {
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

    /*TODO:产生堆外内存溢出*/
    /*springboot2.0后默认使用Lettuce作为操作redis的客户端，使用Netty进行网络通信*/
    /*Lettuce的bug导致netty堆外内存溢出，如果没有堆外内存，默认使用-Xmx300m*/
    /*不能只调大堆外内存，升级Lettuce，切换使用jedis*/

    /*解决缓存穿透：空结果保存*/
    /*解决缓存雪崩，设置随机过期时间*/
    /*加锁，解决缓存击穿*/
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJsonOld() {

        /*缓存都存JSON，跨平台通用*/
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        Map<String, List<Catelog2Vo>> catalog;
        if (StringUtils.isEmpty(catalogJSON)) {
            /*缓存中没有从数据库查*/
            catalog = getCatalogJsonFromDBWithRedisson();
            /*转为JSON保存到缓存（这里写入缓存要放到带锁的方法中去）*/
//            String s = JSON.toJSONString(catalog);
//            redisTemplate.opsForValue().set("catalogJSON", s,1, TimeUnit.DAYS);
        } else {
//            System.out.println("缓存命中");
            catalog = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }

        return catalog;
    }

    /*从数据库查得二级分类数据*/
    /*这里采用Redisson框架加锁*/
    /*缓存的数据一致性问题*/
    /*双写模式 有产生脏数据的风险 看ppt*/
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisson() {

        /*锁的名字牵扯到锁的粒度*/
        /*如果具体的是某个数据，11号商品，product-11-lock*/
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();

        System.out.println("获得分布式锁成功");
        Map<String, List<Catelog2Vo>> dataFromDB;
        try{
            dataFromDB = getDataFromDB();
        }finally {
            lock.unlock();
        }
        return dataFromDB;

    }

    /*从数据库查得二级分类数据*/
    /*这里采用自己向redis中写分布式锁的方式*/
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {

        /*占redis的分布式锁,2.原子性操作加锁+设置过期时间，但是这样又会导致删锁的问题(删的时候锁已经过期）*/
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "111",300,TimeUnit.SECONDS);

        /*3.给锁的值加上uuid，保证是自己的锁，还是有问题，锁已过期别人进来重新加锁了*/
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,300,TimeUnit.SECONDS);

        if(lock){
            System.out.println("获得分布式锁成功");
            /*1.设置锁的过期时间，如果业务出问题不会出现死锁，但是设置锁和加过期时间之间宕机，死锁，因此要保证加锁和设时间的原子性*/
//            redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDB ;


//            redisTemplate.delete("lock");
            /*lua脚本删锁，原子性*/
            try{
                dataFromDB = getDataFromDB();
            }finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            return dataFromDB;
        }else{
            /*加锁失败，自旋的方式*/
            /*休眠100ms*/
            System.out.println("获取分布式锁失败，睡眠200ms");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }


    }

    /*从数据库查得二级分类数据*/
    /*本地锁synchronized ReetrantLock，这里采用本地锁实现*/
    public synchronized Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithSyn() {
        return getDataFromDB();
    }

    /*查数据库原生方法*/
    /*这里用双写模式还有有缓存中的数据不一致的问题*/
    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        /*得到锁之后，再去缓存中确定一次（这里有一个问题，存入redis需要一定时间，使得后来线程进入该方法后redis依然没有数据，因此要把放入redis的操作也写进来）*/
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> catalog = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
//            System.out.println("缓存命中。。。。。。");
            return catalog;
        }

        System.out.println("缓存不命中，查询数据库");
        /*多次查库变成一次查库*/
        List<CategoryEntity> categorys = baseMapper.selectList(null);
        /*查询所有的一级分类ID*/
        List<CategoryEntity> level1Categorys = getParent_cid(categorys, 0L);

        /*封装数据*/
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            /*根据每个一级分类，查到这个一级分类的二级分类*/
            List<CategoryEntity> categoryEntities = getParent_cid(categorys, v.getCatId());
            /*封装结果*/
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(level2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());

                    /*封装三级分类*/
                    List<CategoryEntity> level3Catelogs = getParent_cid(categorys, level2.getCatId());
                    if (level3Catelogs != null) {
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

        /*将数据写入缓存*/
        String s = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

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
