package com.atguigu.gulimall.order;

//import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

//    @Test
//    public void test() {
//        OrderTo orderTo = new OrderTo();
//        orderTo.setId(1234L);
//        rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
//    }

    /**
     * 发送消息
     * 如果发送的消息是对象，我们会使用序列化机制，将对象发送出去
     * 所以要求对象必须实现Serializable
     *  请看RabbitController
     */
    @Test
    public void sendMessage() {

        //发送消息
        String msg = "hello World";
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("哈哈" + i);
                //new CorrelationData(UUID.randomUUID().toString())消息的唯一id
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
            }else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                //模拟失败
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello222.java", orderEntity, new CorrelationData(UUID.randomUUID().toString()));
            }
            log.info("消息发送完成");
        }
    }


    /**
     * 创建 Exchange
     * hello-java-exchange
     */
    @Test
    public void creatExchange() {

        // DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    /**
     * 创建 Queue
     * hello-java-queue
     */
    @Test
    public void creatQueue() {

        //public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    /**
     * 创建 Binging
     * hello.java
     */
    @Test
    public void creatBinging() {

        // public Binding(String destination,目的地
        // DestinationType destinationType,目的地类型
        // String exchange,交换机
        // String routingKey, 路由key
        // map<String,Object> arguments 自定义参数

        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }
}
