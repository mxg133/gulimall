package com.atguigu.gulimall.seckill;

import com.atguigu.common.to.mq.SeckillOrderTo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSeckillApplicationTests {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    public void contextLoads() {
        SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", 1);
    }

}
