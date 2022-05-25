package com.hyl.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hyl.gulimall.product.dao.AttrGroupDao;
import com.hyl.gulimall.product.dao.SkuSaleAttrValueDao;
import com.hyl.gulimall.product.entity.BrandEntity;
import com.hyl.gulimall.product.service.BrandService;
import com.hyl.gulimall.product.service.CategoryService;
import com.hyl.gulimall.product.vo.skuItemvo.SkuItemSaleAttrsVo;
import com.hyl.gulimall.product.vo.skuItemvo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void test1() {
        List<SkuItemSaleAttrsVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(3L);
        for(SkuItemSaleAttrsVo vo:saleAttrsBySpuId) System.out.println(vo);
    }

    @Test
    public void findPaths(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    public void testRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello","wprld");
        System.out.println(ops.get("hello"));
    }

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("华为");
//        brandService.save(brandEntity);
//        System.out.println("保存成功");

//        brandService.updateById(brandEntity);

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach((item)->{
            System.out.println(item);
        });
    }

    @Test
    public void test() {
        System.out.println(0-Integer.MIN_VALUE);
    }

}
