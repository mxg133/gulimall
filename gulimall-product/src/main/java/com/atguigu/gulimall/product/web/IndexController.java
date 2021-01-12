package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 孟享广
 * @date 2021-01-06 12:02 下午
 * @description
 */

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String  indexPage(Map<String, List<CategoryEntity>> map) {

        List<CategoryEntity> entities = categoryService.getLevel1Catrgorys();
        map.put("categorys", entities);
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    //压力测试
    //redisson锁测试
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1 获取一把锁 （只要锁名一样，就是同一把锁）
        RLock lock = redisson.getLock("my-lock");

        //2 加锁
//        lock.lock();//阻塞式等待 默认加的锁 都是30s
        //1） 锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心担心业务时间长，锁自动过期被删掉
        //2） 加锁的业务只要运行完成，不会给当前锁续期，即使不手动解锁 锁也会在30s以后自动删除

        lock.lock(10, TimeUnit.SECONDS); //10秒自动解锁，自动解锁时间一定要大于业务的执行时间
        //1) 如果我们传递了锁的超时时间，就发送给redis执行脚本进行占锁，默认超时时间就是我们指定的时间
        //2) 如果我们没有指定锁的超时时间，就使用30*1000 【看门狗默认时间】
        //      只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗默认时间】每隔20秒自动续期，续成30s
        //      【看门狗时间】3，10s

        //最佳实战
        //1） 推荐lock.lock(30, TimeUnit.SECONDS); 省掉了续期操作。手动解锁
        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("解锁...." + Thread.currentThread().getId());
            //3 解锁  假设解锁代码没有运行，redisson会不会出现死锁
            lock.unlock();
        }
        return "hello";
    }

    //保证一定读到最新数据， 修改期间，写锁是一个排他锁（互斥锁） 读锁是一个共享锁
    //写锁没释放，读就必须等待
    // 读锁 都可以读取到数据
    // 写锁 -》读锁读取不到数据 写锁完成后 才能读取到数据 保证数据最新 一致性
    // 读 + 读 无锁模式 并发读 只会在redis中记录好 所有当前读锁他们都会加锁成功
    // 写 + 读 等待写锁释放
    // 写 + 写 阻塞方式
    // 读 + 写 有读锁 写也需要等待
    // 只要有写的存在 都需要等待 （我写着呢 你读个几把啊）
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            //1 改数据加写锁，读数据加读锁
            rLock.lock();
            System.out.println("写锁加锁成功..." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放..." + Thread.currentThread().getId());
        }

        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        //加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功..." + Thread.currentThread().getId());
            s = redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放..." + Thread.currentThread().getId());
        }

        return s;
    }


    /**
     * 信号量
     * 车库停车
     * 3 车位
     * 信号量可以作为分布式限流
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {

        RSemaphore park = redisson.getSemaphore("park");
        //acquire()是阻塞式获取，我一定要获取一个车位
//        park.acquire();//获取一个信号 获取一个值 占一个车位

        boolean b = park.tryAcquire();//有了就占 没了就算了
        if (b) {
            //执行业务

        }else {
            //直接返回
            return "error";
        }
        return "ok" + b;
    }

    /**
     * 车库开走车
     * 3 车位
     */
    @ResponseBody
    @GetMapping("/go")
    public String go() throws InterruptedException {

        RSemaphore park = redisson.getSemaphore("park");
        park.release();//释放一个信号 释放一个值 释放一个车位

        return "ok";
    }

    /**
     * 闭锁
     * 放假、锁门
     * 1 班没人了， 2···
     * 5个班 全部走完 我们才可以占锁
     */
    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);//等待5个锁都锁
        door.await();//等待闭锁完成 等待锁的数量为0
        return "放假成功....";
    }

    /**
     * 闭锁
     * 放假、锁门
     */
    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id) throws InterruptedException {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//计数-1，锁的数-1 (i--)
        return id + " 走了....";
    }
}
