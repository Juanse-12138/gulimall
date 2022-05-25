package com.hyl.gulimall.product.dao;

import com.hyl.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:02:16
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
