package com.hyl.gulimall.coupon.dao;

import com.hyl.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 18:38:27
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
