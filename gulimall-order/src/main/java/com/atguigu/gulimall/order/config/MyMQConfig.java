package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-11 1:08 下午
 * @description 创建RabbitMQ 队列 交换机
 *      运行之前，一定要小心，否则要删除队列/交换机重新运行 麻烦！
 */
@Configuration
public class MyMQConfig {

    @RabbitListener(queues = "order.release.order.queue")
    public void listening(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单，准备关闭订单。order："+entity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    //容器中的组建Queue Exchange Binding 都会自动创建（前提是RabbitMQ没有）
    @Bean
    public Queue orderDelayQueue() {

        // String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");//死信交换机
        arguments.put("x-dead-letter-routing-key", "order.release.order");//死信路由键
        arguments.put("x-message-ttl", 60000);//消息过期时间 ms 1分钟
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue() {

        //普通队列
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange() {

        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        //普通交换机
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {

        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                new HashMap<>());
    }

    @Bean
    public Binding orderReleaseOrderBinding() {

        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order", new HashMap<>());
    }

//    @Bean
//    public Binding orderReleaseOtherBinding() {
//
//        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
//                "order-event-exchange", "order.release.other.#",
//                new HashMap<>());
//    }

//    @Bean
//    public Queue orderSeckillOrderQueue() {
//        return new Queue("order.seckill.order.queue", true, false, false);
//    }
//
//    @Bean
//    public Binding orderSeckillOrderQueueBinding() {
//        return new Binding("order.seckill.order.queue",
//                Binding.DestinationType.QUEUE,
//                "order-event-exchange",
//                "order.seckill.order",
//                new HashMap<>());
//    }
}
