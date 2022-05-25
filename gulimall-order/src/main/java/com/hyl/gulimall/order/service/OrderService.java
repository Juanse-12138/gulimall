package com.hyl.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.to.mq.SeckillOrderTo;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.order.entity.OrderEntity;
import com.hyl.gulimall.order.vo.OrderConfirmVo;
import com.hyl.gulimall.order.vo.OrderSubmitVo;
import com.hyl.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;

/**
 * 订单
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 19:10:25
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity order);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}

