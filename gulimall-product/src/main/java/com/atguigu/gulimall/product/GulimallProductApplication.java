package com.atguigu.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1、整合MyBatis-Plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *      </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动。https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-versions.html
 *              2）、在application.yml配置数据源相关信息
 *          2、配置MyBatis-Plus；
 *              1）、使用@MapperScan
 *              2）、告诉MyBatis-Plus，sql映射文件位置
 *
 * 2、逻辑删除
 *  1）、配置全局的逻辑删除规则（省略）
 *  2）、配置逻辑删除的组件Bean（省略）
 *  3）、给Bean加上逻辑删除注解@TableLogic
 *
 * 3、JSR303
 *   1）、给Bean添加校验注解:javax.validation.constraints，并定义自己的message提示
 *   2)、开启校验功能@Valid
 *      效果：校验错误以后会有默认的响应；
 *   3）、给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *   4）、分组校验（多场景的复杂校验）
 *         1)、	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *         2）、@Validated({AddGroup.class})
 *         3)、默认没有指定分组的校验注解@NotBlank，在分组校验情况@Validated({AddGroup.class})下不生效，只会在@Validated生效；
 *
 *   5）、自定义校验
 *      1）、编写一个自定义的校验注解
 *      2）、编写一个自定义的校验器 ConstraintValidator
 *      3）、关联自定义的校验器和自定义的校验注解
 *      @Documented
 * @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器，适配不同类型的校验】 })
 * @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
 * @Retention(RUNTIME)
 * public @interface ListValue {
 *
 * 5.模板引擎
 *  1)、thymeleaf-starter 关闭缓存
 *  2)、静态资源都放在static文件夹下就可以按照路径直接访问
 *  3)、页面放在templates下 直接访问
 *       SpringBoot 访问项目的时候，默认会找index
 * 6.整合redis
 *  1）、引入data-redis-starter
 *  2) 、简单配置redis的host信息
 *  3) 、使用springBoot自动配置好的StringRedisTemplate来操作redis
 *   redis-map 存放数据key 数据值value
 *
 * 7、redisson配置
 *       引入依赖
 *  <dependency>
 *      <groupId>org.redisson</groupId>
 *      <artifactId>redisson</artifactId>
 *      <version>3.12.0</version>
 *  </dependency>
 *
 * 8、整合SringCache 简化缓存开发
 *       1）、引入依赖
 *       spring-boot-starter-cache、spring-Boot-starter-data-redis
 *       2）、写配置
 *           1）自动配置了那些？
 *           CacheAutoConfiguration 回导入 RedisCacheConfiguration
 *           自动配置好了缓存管理器RedisCacheManage
 *           2)配置使用redis作为缓存
 *           spring.cache.type=redis
 *       3）、测试使用缓存
 *          @Cacheable 保存 触发将数据保存到缓存操作
 *          @CacheEvict 删除 触发将数据从缓存删除的操作
 *          @CachePut 更新 不影响方法执行更新操作
 *          @Caching 组合 组合以上多个操作
 *          @CacheConfig 在类级别共享缓存的相同配置
 *       1)、开启缓存功能
 *           @EnableCaching
 *       2）、只需要使用注解
 *       3）、将数据保存为json格式
 *           自定义RedisCacheConfiguration
 *  4.Spring-cache的不足：
 *       1）、读模式
 *           缓存穿透：查询一个null的数据 解决：缓存空数据 cache-null-value=true
 *           缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁 默认是无加锁的 sync=true
 *           缓存雪崩：大量key同时过期 解决 加随机时间，加上过期时间，spring-cache-redis-time-to
 *       2）、写模式： 缓存与数据一致
 *           1）、读写枷锁
 *           2）、引入Canal 感知到MySQL的更新去更新数据库
 *           3）、读多写多，直接去数据库查询就行
 *       总结：
 *       常规数据 读多写少 即时性，一致性要求不高的数据 完全可以使用Spring-cache：写模式 只要缓存数据有过期时间就行
 *       原理：
 *       cacheManage(RedisCacheManage) -> Cache(RedisCache)->Cache复制缓存的读写
 *
 * 4、统一的异常处理
 * @ControllerAdvice
 *  1）、编写异常处理类，使用@ControllerAdvice。
 *  2）、使用@ExceptionHandler标注方法可以处理的异常。
 */

//开启feign客户度的远程调用功能
//扫描feign文件夹下的带有@FeignClient注解的接口
@EnableFeignClients(basePackages = "com.atguigu.gulimall.product.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
