package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-06 11:47 上午
 * @description创建RabbitMQ 队列 交换机
 * 运行之前，一定要小心，否则要删除队列/交换机重新运行 麻烦！
 */
//开启RabbitMQ消息队列
@EnableRabbit
@Configuration
public class MyRabbitMQConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

//    @RabbitListener(queues = "order.release.order.queue")
//    public void listening(OrderEntity entity, Channel channel, Message message) throws IOException {
//        System.out.println("收到过期的订单，准备关闭订单。order："+entity.getOrderSn());
//        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//    }

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

        //和延时队列绑定
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                new HashMap<>());
    }

    @Bean
    public Binding orderReleaseOrderBinding() {

        //和普通队列绑定
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                new HashMap<>());
    }

    @Bean
    public Binding orderReleaseOtherBinding() {

        //订单释放直接和库存失望进行绑定
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.other.#",
                new HashMap<>());
    }

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

    /**
     * 下面全都是基础配置
     */

    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制rabbitTemplate
     * 1.publisher-confirms: true
     * 3.消费端确认 (保证每个消息被正确消费 此时才可以braker删除这个消息)
     * 1.默认是自动确认的 只要消息接收到  客户端自动确认服务端就要移除这个消息
     * 问题 ：
     * 收到很多消息 自动回复给服务器ack 只有一个消息处理成功 宕机了 发现消息丢失
     * 手动确认模式： 只要我们没有确认高随MQ 货物被签收 没有ack
     * 消息就一直是unacked状态 即使Consumer宕机 消息不会丢失 会重新变成ready
     * 2.如果签收
     */
    @PostConstruct  //MyRabbitConfig对象创建完成以后执行这个方法
    public void initRabbitTemplate() {

        //设置确认回调 消息到了队列
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {

            /**
             * 1、消息抵达服务器 ack=true
             * @param correlationData 当前消息唯一关联的数据 这个是消息爱的唯一id
             * @param ack 消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //服务器收到了
                System.out.println("消息抵达服务器confirm....correlationData[" + correlationData + "]==>ack[" + ack + "]cause>>>" + cause);
            }
        });

        //设置消息队列的确认回调 发送了，但是队列没有收到
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            /**
             * 只要消息没有投递给指定的队列 就触发这个失败回调
             * @param message  投递失败的消息详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange 当时这个消息发给那个交换机
             * @param routingKey 当时这个消息用那个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //报错误 未收到消息
                System.out.println("Fail!! Message[" + message + "]==>[" + exchange + "]==>routingKey[" + routingKey + "]");
            }
        });
    }
}
