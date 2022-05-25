package com.hyl.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hyl.common.to.mq.OrderTo;
import com.hyl.common.to.mq.StockLockedTo;
import com.hyl.common.utils.PageUtils;
import com.hyl.gulimall.ware.entity.WareSkuEntity;
import com.hyl.gulimall.ware.vo.SkuHasStockVo;
import com.hyl.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author hyl
 * @email heyinlong1998@163.com
 * @date 2022-03-07 19:17:39
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unlock(StockLockedTo to) throws IOException;

    void unLockStockForOrder(OrderTo to);
}

