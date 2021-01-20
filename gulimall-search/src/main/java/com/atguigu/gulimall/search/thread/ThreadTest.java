package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author 孟享广
 * @date 2021-01-20 10:49 上午
 * @description
 */
public class ThreadTest {

    //线程池 每个系统1 - 2个
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
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
        service.execute(new Runable01());

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
