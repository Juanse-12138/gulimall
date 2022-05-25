package com.hyl.gulimall.ware.dao;

import com.hyl.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sun.org.glassfish.gmbal.ParameterNames;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 19:17:39
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(Long skuId);

    List<Long> listWareIdHasSkuStock(Long skuId);

    Long lockSkuStock(Long skuId, Long wareId, Integer num);

    void unlockStock(@Param("skuId") Long skuId, @Param("num") Integer skuNum, @Param("wareId") Long wareId, @Param("taskDetailId") Long taskDetailId);
}
