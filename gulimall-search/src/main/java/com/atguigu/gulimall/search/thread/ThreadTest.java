package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author 孟享广
 * @date 2021-01-20 10:49 上午
 * @description
 */


public class ThreadTest {

    //线程池 每个系统1 - 2个
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        //main1() 所有注释都已经打开 不能执行。需要测试的话，复制到这里。
        System.out.println("开始");




        System.out.println("结束");
    }

    public static void main1(String[] args) throws ExecutionException, InterruptedException {

        System.out.println("开始");

        //异步编排1 无返回值runAsync()
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("结果运行：" + i);
        }, executor);

        //异步编排2 有返回值 supplyAsync()
        //方法完成后的感知
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("结果运行：" + i);
            return i;
        }, executor).whenComplete((result, exception)->{
            //虽然能得到异常信息，却不能修改返回数据，类似监听器
            System.out.println("异步任务完成了，结果是：" + result + "，异常是：" + exception);
        }).exceptionally((throwable -> {
            //可以感知异常，同时返回默认数据
            return 10;
        }));
        System.out.println(future2.get());

        //handle()方法执行后的处理(无论成功完成还是失败完成) 就算有异常 也想要结果
        CompletableFuture<Integer> future2_1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("结果运行：" + i);
            return i;
        }, executor).handle((result, throwable)->{
            if (result != null) {
                return result * 2;
            }
            if (throwable != null) {
                //出现了异常
                return 0;
            }
            return 0;
        });
        System.out.println(future2_1.get());

        /**
         * 串行化 A任务完成后 -> B任务执行
         * 带Async的意思是：再开一个线程； 否则和A线程共用一个线程
         */

        //thenRunAsync() 不能获取到上一步的执行结果 无返回值
        CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("结果运行：" + i);
            return i;
        }, executor).thenRunAsync(() -> {
            System.out.println("任务2启动了");
        }, executor);

        //thenAcceptAsync() 能接受上一个任务的结果，但是无返回值
        CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("结果运行：" + i);
            return i;
        }, executor).thenAcceptAsync((result)->{
            System.out.println("任务2启动了" + "上一步执行的结果是：" + result);
        }, executor);

        //thenAcceptAsync() 能接受上一个任务的结果，有返回值
        CompletableFuture<String> future2_2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("结果运行：" + i);
            return i;
        }, executor).thenApplyAsync((result) -> {
            System.out.println("任务2启动了" + "上一步执行的结果是：" + result);
            return "hello" + result;
        }, executor);
        System.out.println(future2_2.get());

        /**
         * 两个任务组合
         * 两个任务都完成 然后执行第三个 A + B -> C
         */
        CompletableFuture<Object> future3_1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始。当前线程：" + Thread.currentThread().getId());
            int i = 10 / 4;
            System.out.println("任务1结束。结果运行：" + i);
            return i;
        }, executor);
        CompletableFuture<Object> future3_2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始。当前线程：" + Thread.currentThread().getId());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务2结束...");
            return "hello";
        }, executor);

        //runAfterBothAsync()不能感知前两步的执行结果 自己也没有返回值
        future3_1.runAfterBothAsync(future3_2, ()->{
            System.out.println("任务3开始。");
        }, executor);

        //thenAcceptBothAsync()能感知前两步的执行结果 自己没有返回值
        future3_1.thenAcceptBothAsync(future3_2, (f1, f2)->{
            System.out.println("任务3开始。之前的结果：f1=" + f1 + "；f2=" + f2);
        }, executor);

        //thenCombineAsync()能感知前两步的执行结果， 还能处理前面两个任务的返回值，并生成返回值 自己有返回值
        CompletableFuture<String> future3_3 = future3_1.thenCombineAsync(future3_2, (f1, f2) -> {
            return f1 + ": " + f2 + "->ww";
        }, executor);
        System.out.println(future3_3.get());

        /**
         * 两个任务 只要有一个完成就行 就能执行第三个任务 A || B = C
         */
        //runAfterEitherAsync() 不感知前面任务的结果，自己也没有返回值
        future3_1.runAfterEitherAsync(future3_2, () -> {
            System.out.println("任务3开始执行。");
        }, executor);

        //acceptEitherAsync() 能感知前面任务的结果，自己没有返回值
        future3_1.acceptEitherAsync(future3_2, (result) -> {//要求任务1、2的返回类型必须相同
            System.out.println("任务3开始执行。" + result);
        }, executor);

        //applyToEitherAsync() 能感知前面任务的结果，自己有返回值
        CompletableFuture<String> future3_4 = future3_1.applyToEitherAsync(future3_2, (result) -> {//要求任务1、2的返回类型必须相同
            System.out.println("任务3开始执行。" + result);
            return result.toString() + "哈哈";
        }, executor);
        System.out.println(future3_4.get());

        /**
         * 多任务组合
         */
        CompletableFuture<String> future4_1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息……");
            return "hello.jpg";
        }, executor);
        CompletableFuture<String> future4_2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性……");
            return "黑色 2+64G";
        }, executor);
        CompletableFuture<String> future4_3 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("查询商品的介绍……");//模拟业务时间超长
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "apple";
        }, executor);

        //allOf() 所有任务结束后才能继续执行
        CompletableFuture<Void> future4_4 = CompletableFuture.allOf(future4_1, future4_2, future4_3);
        future4_4.get();//等待上面3个任务完成 不可以打印
        System.out.println(future4_1.get() + future4_2.get() + future4_3.get());

        //anyOf() 只要一个任务，结束就可以继续执行
        CompletableFuture<Object> future4_5 = CompletableFuture.anyOf(future4_1, future4_2, future4_3);
        future4_5.get();//等待所有任务完成 可以打印
        System.out.println(future4_5.get());

        System.out.println("结束");
    }

    public static void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("开始");

        //方法一
//        Thread01 thread01 = new Thread01();
//        thread01.start();

        //方法二
//        Runable01 runable01 = new Runable01();
//        new Thread(runable01).run();

        //方法三
//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//        //阻塞 前边执行完毕，才可以执行获取返回值这个方法。
//        //等待整个线程执行完成，获取返回结果。
//        Integer integer = futureTask.get();
//        System.out.println(integer);

        //我们以后的业务代码里面，以上三种启动线程的方式禁止使用。
        //【将所有多线异步任务都交给线程池执行】
//        new Thread(()->System.out.println("hello")).start();

        //区别：
        //1 2 不能得到返回值
        //3 可以得到返回值
        //1 2 3 都不能控制资源
        //4 可以控制资源，性能稳定
//        service.execute(new Runable01());
        /**
         * corePoolSize 保留在池中的线程数 即使处于空闲状态 除非设置了allowCoreThreadTimeOut
         *
         * maximumPoolSize *池中允许的最大线程数
         *
         * keepalivueTime 存活时间 如果当前线程大于core的数量
         *          释放空闲的线程 maximumPoolsize-corePoolSize 只要线程空闲大于指定的keepAlivuetime
         * unit:时间单位
         * BlockingQUeue<Runnable> workQueue 阻塞队列 如果任务有很多 就会将目前多的任务放在队列里面
         *      只要有线程空闲，就会去队列里面取出新的任务继续执行
         * threadFactory 线程创建工厂
         * RejectedExecutionHandler 如果队列满了 按照我们指定得拒绝策略拒绝指定任务
         *
         * 工作顺序
         * 1)、线程池创建好 准备好core数量的核心线程，准备接受任务
         * 1.1、core满了 就将在进来的任务放入阻塞队列中 空闲的core就会自己去阻塞队列获取任务执行
         * 1.2、阻塞队列满了 就直接开新线程执行 最大只能开到max指定数量
         * 1.3、max满了就用RejectedExecutionHandler 拒绝任务
         * 1.4、max都执行完成，有很多空闲 指定时间以后keepAlivueTime以后 释放max-core(195)这些线程
         *
         *       new LinkedBlockingQueue<>() 默认是Integer最大值 内存不够
         *
         *       一个线程池 core:7 max:20 queue:50 100并发进来怎么分配
         *       7个会立即得到执行 50个进入队列 再开13个进行执行，剩下的30个就使用拒绝策略
         *       如果不想抛弃还要执行 CallerRunsPolicy 同步方式
         */
        //最原始的方式：
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5,
                200,
                10, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());


        //快速创建线程池
          Executors.newCachedThreadPool(); //core是0 所有都可回收
          Executors.newFixedThreadPool(10);//固定大小 core=max 都不可以回收
          Executors.newScheduledThreadPool(10); //定时任务的线程池
          Executors.newSingleThreadExecutor(); //单线程的线程池,后台从队列里面获取任务 挨个执行

        System.out.println("结束");
    }

    //方法一
    public static class Thread01 extends Thread {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("结果运行：" + i);
        }
    }

    //方法二
    public static class Runable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("结果运行：" + i);
        }
    }

    //方法三
    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("结果运行：" + i);
            return i;
        }
    }
}
