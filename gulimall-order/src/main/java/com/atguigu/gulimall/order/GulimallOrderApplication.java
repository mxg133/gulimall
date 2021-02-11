package com.atguigu.gulimall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 使用 RabbitMQ 消息队列
 * 1 引入依赖
 *      spring-boot-starter-amqp
 * 2 配置文件 配置host port virtual-host
 * 3 加注解
 *      @EnableRabbit
 *
 *  @Transactional 本地事务失效问题
 *  同一个对象内事务互调默认方法 原因 绕过了代理对象 事务使用代理对象来控制
 *  解决：使用代理对象调用事务方法
 *      1）、引入aop-starter; spring-boot-starter-aop 引入aspectj
 *      2）、@EnableAspectjAutoProxy(exposeProxy = true) 开启 aspectj 动态代理功能 以后所有动态代理都是aspectj
 *
 * 解决数据一致性问题
 *  Raft算法 http://thesecretlivesofdata.com/raft/
 *          BASE理论
 *              使用AP
 *              舍弃CP强一致(本地数据库就是)， 使用最终一致即可
 *
 *  分布式事物几种解决方案
 *      1 2PC模式 二级提交协议
 *      2 柔性事物 TCC事务补偿型方案
 *          刚性事务：遵循ACID原则，强一致性原则
 *          柔性事务：遵循BASE理论，最终一致性
 *      3 柔性事务 最大努力通知型方案
 *      4 柔性事务 可靠消息+最终一致性方案（异步确保型）‍️
 *
 * seata 要控制分布式事务 【不用】
 *   1）、每一个微服务必须创建uodo_log
 *   2)、安装事务协调器 seata-server https://github.com/seata.seata/releases
 *   3)、整合
 *        1、导入依赖 spring-cloud-starter-alibaba-seata ｜ seata-all-0.7.1
 *        2、解压并启动seata-server：(TC)
 *            registry.conf 注册中心配置 修改registry type=nacos
 *            file.conf
 *        3、所有想要用到分布式事务的微服务使用seata DataSourceProxy
 *        4、每个服务都导入
 *            registry.conf
 *            file.conf 设置：vgroup_mapping.gulimall-order-fescar-service-group = "default"
 *       5、 大事务：@GlobalTransactional
 *           远程小事务：@Transactional
 *
 * RabbitMQ延时队列 用
 *  TTL 消息存活时间 队列和消息可以一设置TTL
 *      死信：1 被消费者拒收，且不让回归队列，直接丢弃
 *           2 过了时间
 *           3 队列限制满了，老消息被丢弃
 *  延时队列实现-1 【用】
 *      设置【队列】过期时间 实现延时队列
 *  延时队列实现-2 【不用】
 *      设置【消息】过期时间 实现延时队列
 */
//开启aspectj动态代理功能(对外暴露代理对象) 本地事物
@EnableAspectJAutoProxy(exposeProxy = true)
//开启feign客户度的远程调用功能
//扫描feign文件夹下的带有@FeignClient注解的接口
@EnableFeignClients(basePackages = "com.atguigu.gulimall.order.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
