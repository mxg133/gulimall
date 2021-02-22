package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 孟享广
 * @date 2021-02-06 11:47 上午
 * @description
 */
//开启RabbitMQ消息队列
@EnableRabbit
@Configuration
public class MyRabbitMQConfig {

    RabbitTemplate rabbitTemplate;

    //TODO RabbitTemplate
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    //没有这个方法， 不能创建RabbitMQ的东西
//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle(Message message) {
//
//    }

    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange stockEventExchange() {

        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        //普通交换机
        return new TopicExchange("stock-event-exchange", true, false);
    }

    //容器中的组建Queue Exchange Binding 都会自动创建（前提是RabbitMQ没有）
    @Bean
    public Queue stockDelayQueue() {

        // String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");//死信交换机
        arguments.put("x-dead-letter-routing-key", "stock.release");//死信路由键
        arguments.put("x-message-ttl", 120000);//消息过期时间 ms 2分钟
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue stockReleaseOrderQueue() {

        //普通队列
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Binding stockCreateStockBinding() {

        //和延时队列绑定
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }

    @Bean
    public Binding stockReleaseOrderBinding() {

        //和普通队列绑定
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
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
//    @PostConstruct  //MyRabbitConfig对象创建完成以后执行这个方法
//    public void initRabbitTemplate() {
//
//        //设置确认回调 消息到了队列
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//
//            /**
//             * 1、消息抵达服务器 ack=true
//             * @param correlationData 当前消息唯一关联的数据 这个是消息爱的唯一id
//             * @param ack 消息是否成功收到
//             * @param cause 失败的原因
//             */
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                //服务器收到了
//                System.out.println("消息抵达服务器confirm....correlationData[" + correlationData + "]==>ack[" + ack + "]cause>>>" + cause);
//            }
//        });
//
//        //设置消息队列的确认回调 发送了，但是队列没有收到
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//
//            /**
//             * 只要消息没有投递给指定的队列 就触发这个失败回调
//             * @param message  投递失败的消息详细信息
//             * @param replyCode 回复的状态码
//             * @param replyText 回复的文本内容
//             * @param exchange 当时这个消息发给那个交换机
//             * @param routingKey 当时这个消息用那个路由键
//             */
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//                //报错误 未收到消息
//                System.out.println("Fail!! Message[" + message + "]==>[" + exchange + "]==>routingKey[" + routingKey + "]");
//            }
//        });
//    }
}
