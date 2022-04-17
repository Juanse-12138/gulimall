package com.hyl.gulimall.order.dao;

import com.hyl.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 19:10:25
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
