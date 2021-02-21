package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.utils.AlipayTemplate;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author 孟享广
 * @date 2021-02-21 5:53 下午
 * @description
 */
@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSeckillListener {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void listening(SeckillOrderTo seckillOrderTo, Channel channel, Message message) throws IOException {

        try {
            log.info("创建秒杀单的相信信息. ...");
            orderService.creatSeckillOrder(seckillOrderTo);
            //手动调用支付宝收单 p310 暂时不用手动
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            //true 重新回到消息队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
