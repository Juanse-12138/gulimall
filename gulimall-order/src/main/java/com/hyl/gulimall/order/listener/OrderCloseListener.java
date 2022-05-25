package com.hyl.gulimall.order.listener;

import com.hyl.gulimall.order.entity.OrderEntity;
import com.hyl.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity order, Channel channel, Message message) throws IOException {
        orderService.closeOrder(order);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
