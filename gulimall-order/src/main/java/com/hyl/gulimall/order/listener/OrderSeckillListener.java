package com.hyl.gulimall.order.listener;

import com.hyl.common.to.mq.SeckillOrderTo;
import com.hyl.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {

        try {
            log.info("顺被创建秒杀订单");
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
