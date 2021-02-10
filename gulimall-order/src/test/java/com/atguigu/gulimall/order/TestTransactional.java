package com.atguigu.gulimall.order;

import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author 孟享广
 * @date 2021-02-10 11:10 上午
 * @description
// */
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class TestTransactional {

    /**
     * 不共用事物设置 就放在不同的文件
     */
    @Transactional(timeout = 30)
    public void a() {
        b();//共用一个事物
        c();//新事物
        int i = 1 / 0;//a b回滚 c不回滚

        TestTransactional test = (TestTransactional) AopContext.currentProxy();

        //通过代理对象，调用的b()和c()，他们的事物设置才有用
        test.b();
        test.c();

    }

    /**
     * timeout = 2 失效
     * b()继承了a()的设置
     */
    @Transactional(propagation = Propagation.REQUIRED, timeout = 2)
    public void b() {
        //7s
    }

    /**
     * 扛把子
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void c() {

    }
}
