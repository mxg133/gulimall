package com.atguigu.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 秒杀（高并发）系统关注的问题
 *
 * 01服务单一职责+独立部署
 *      秒杀服务即使自己扛不住压力，挂掉。不要影别人
 *
 * 02秒杀链接加密
 *      防止恶意攻击，模拟秒杀请求。1000次/s攻击。防止链接暴露，自己工作人员，提前秒杀商品
 *
 * 03库存预热+快速扣减
 * 04动静分离
 * ....
 *
 *
 * 1 整合Sentinel
 *  1) 引入依赖spring-cloud-starter-alibaba-sentinel
 *  2) 下载sentinel控制台 sentinel-dashboard-1.6.3.jar
 *  3) 配置sentinel控制台地址信息
 *  4) 在控制台调整参数 [默认所有的留空设置保存在内存,重启失效]
 *
 * 2 每一个微服务都引入统计审计信息spring-boot-starter-actuator
 *     并配置management.endpoints.web.exposure.include=*
 *
 * 3 流控模式&效果 全服务引入
 *
 * 4 使用Sentinel来保护Feign远程调用：熔断机制
 *     1) 调用方开启熔断保护
 *     2) 调用手动指定远程服务的降级策略 远程服务被降级处理，出发我们的熔断回调方法
 *     3) 超大浏览的时候，必须牺牲一些远程服务。在服务的提供方（远程服务）指定降级策略，
 *         提供方是在运行，但使不运行自己的业务逻辑，返回他是默认的降级数据（限流的数据）
 *
 * 5 自定义受保护的资源
 *    1) try(Entry entry = SphU.entry("seckillSkus")) {
 *        ///业务逻辑
 *    }
 *    2）基于注解
 *       @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
 *  ** 1 和 2   一定要配置被限流以后的默认返回
 *     url      请求可以设置统一返回
 */
//开启feign客户度的远程调用功能
//扫描feign文件夹下的带有@FeignClient注解的接口
@EnableFeignClients(basePackages = "com.atguigu.gulimall.seckill.feign")
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
