package com.hyl.gulimall.ware.listener;

import com.hyl.common.to.mq.OrderTo;
import com.hyl.common.to.mq.StockLockedTo;
import com.hyl.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    @RabbitHandler
    public void handlerStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("************************收到超时库存解锁的消息********************************");
        try {
            wareSkuService.unlock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo to, Message message, Channel channel) throws IOException {
        log.info("************************订单关闭准备解锁库存********************************");
        try {
            wareSkuService.unLockStockForOrder(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
