package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderItemDao;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.service.OrderItemService;

//这个类能接受hello-java-queue消息
@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 监听消息
     * queues 声明需要监听的所有队列
     * org.springframework.amqp.core.Message
     * <p>
     * 参数可以写一下类型
     * 1、Message essage: 原生消息详细信息。头+体
     * 2、发送的消息的类型: OrderReturnReasonEntity content;
     * 3、Channel channel:当前传输数据的通道
     * <p>
     * Queue:可以很多人都来监听,只要收到消息,队列删除消息,而且只能有一个收到此消息
     * 1)、订单服务启动多个：同一个消息,只能有一个客户端收到
     * 2)、只有一个消息完全处理完,方法运行结束，我们就可以接收到下一个消息
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    //这个类的这个方法才能接受hello-java-queue消息
    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity content, Channel channel) {

        //拿到消息体
//        byte[] body = message.getBody();
        //拿到消息头
//        MessageProperties properties = message.getMessageProperties();

        System.out.println("接收到消息:" + content);

        //消息处理完 手动确认  deliveryTag在Channel内按顺序自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag->" + deliveryTag);

        try {
            if (deliveryTag % 2 == 0) {
                //确认签收 队列删除该消息 false非批量模式
                channel.basicAck(deliveryTag, false);
            } else {
                //拒收退货 第三个参数 -> true:重新入队 false:丢弃
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (IOException e) {
            //网络中断
        }
    }

    //    @RabbitHandler
    public void receiveMessage2(OrderEntity content) {

        System.out.println("接收到消息:" + content);
    }
}