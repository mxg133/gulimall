package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装父子的树形结构
        //2.1 找到所有一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
            categoryEntity.getParentCid() == 0
        ).map(menu->{
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前删除的菜单，是否被别的地方引用

        baseMapper.deleteBatchIds(asList);
    }

    //拿到完整路径
    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    //存储同一类型的数据，都可以指定成同一个分区 分区名默认就是缓存的前缀
    //失效模式 删除category区下【所有】的数据
    @CacheEvict(value = "category", allEntries = true)
    //同时进行多种缓存操作 组合删除
//    @Caching(evict = {
//            //失效模式：修改删除缓存
//            @CacheEvict(value = "category", key = "'getLevel1Catrgorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    //双写模式 修改后再放入缓存
//    @CachePut
    @Transactional
    //级联更新 所有数据
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

        //同时修改缓存中的数据
        //redis.del(catalogJSON);等待下次主动查询进行更新

    }

    /**
     * @Cacheable 表示这个东西是结果是需要缓存的，如果缓存中有，方法不调用；否则调用方法，放入缓存
     * value 每一个需要缓存的数据我们都要来指定要放在哪个名字的缓存里【缓存的分区(按照业务类型区分)】
     * 3)、默认行为
     *   1）、如果缓存中有 方法不用调用
     *   2）、 key默认生成 缓存的名字::simplekey 自动生成的key值
     *   3）、缓存的value值 默认使用java虚拟化机制 将序列化的数据存到redis
     *   4）、默认过期时间为-1
     *
     *  自定义
     *   1）、指定生成的缓存使用的key key的属性指定接受一个SpEL表达式
     *       SpEL表达式地址
     *   2)、指定缓存的数据存活时间 配置文件中修改TTL
     *   3）、将数据修改为json格式
     */ //sync = true解决缓存击穿 默认是false
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Catrgorys() {

        //测试spring cache
        System.out.println("public List<CategoryEntity> getLevel1Catrgorys() {....");

        //压力测试  数据库navicat增加了 parent_cid 为索引
//        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
//        System.out.println("消耗时间->：" + (System.currentTimeMillis()-l) + "ms");
        return categoryEntities;
//        return null;//测试缓存空值
    }

    @Override
    @Cacheable(value = "category", key = "#root.methodName")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询数据库...");
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //1 查出所有1级分类
        List<CategoryEntity> level1Catrgorys = getParent_cid(selectList, 0L);

        //2 封装分类
        Map<String, List<Catelog2Vo>> parent_cid = level1Catrgorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1 拿到每一个1级分类 查到这个1级分类的2级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2 封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1 找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catalog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2 封装成指定格式
                            Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return parent_cid;
    }

    //TODO 产生堆外内存溢出：OutOfDirectMemoryError
    //1)、Springboot2.0以后默认使用Lettuce作为操作redis的客户端。它使用netty进行网络通信。
    //2), Lettuce的bug导致netty堆外内存溢出 -Xmx300m; netty如果没有指定堆外内存，默认使用Xmx300m
    //      可以通过-Dio.netty.maxDirectMemory进行设置
    //解决方案  不能使用-Dio.netty.maxDirectMemory只是去调大堆外内存。
    //        1)、升级Lettuce客户端。 2),切换使用jedis
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        //给缓存中放入json字符串，拿出的json字符串，还要逆转为能用的对象【序列化与反序列化】

        /**
         * 1、空结果缓存:解决缓存穿透
         * 2、设置过期时间(加随机值):解决缓存雪崩
         * 3、加锁;解决缓存击穿
         */

        //1 加入缓存逻辑， 以后缓存中存放的都是json字符串   json跨平台、跨语言兼容
        String catalogJson = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJson)) {
            //2 缓存中没有，查询数据库。
            System.out.println("缓存不命中！。。。。查询数据库。。");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            return catalogJsonFromDb;
        }
        System.out.println("缓存命中！。。。。直接返回。");

        //转为指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        return result;
    }

    /**
     * 使用分布式锁
     * 从数据库查询并封装分类数据
     *
     * 缓存一致性问题
     * 缓存里面的数据如何和数据库里面的数据保持一致？
     * 1） 双写模式 数据库改完后，缓存也改
     * 2） 失效模式 数据库改完后，把缓存删掉
     *
     * 缓存数据一致性-解决方案
     * 无论是双写模式还是失效模式,都会导致缓存的不一致问题,即多个实例同时更新会出事,怎么办?
     * 1、如果是用户纬度数据(订单数据、用户数据),这种并发几率非常小,不用考虑这个问题,缓存数据加上过期时间,每隔一段时间触发读的主动更新即可
     * 2、如果是菜单,商品介绍等基础数据,也可以去使用canal订阅binlog的方式。
     * 3、缓存数据+过期时间也足够解决大部分业务对于缓存的要求。
     * 4、通过加效保证并发读写,写写的时候按顺序排好队,读读无所谓,所以适合使用读写锁,(业务不关心脏数据,允许临时脏数据可忽略);
     * 总结。
     * 我们能放入缓存的数据本就不应该是实时性、一致性要求超高的,所以缓存数据的时候加上过期时间,保证每天拿到当前最新数据即可,
     * 我们不应该过度设计,增加系统的复杂性
     * 遇到实时性、一致性要求高的数据,就应该查数据库,即使慢点。
     *
     * 我们系统的一致性解决方案:
     * 1、缓存的所有数据都有过期时间,数据过期下一次查询触发主动更新
     * 2、读写敌据的时候,加上分布式的读写锁。
     *          经常写,经常读
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        //1 锁的名字，锁的粒度，越细越快
        RLock lock = redisson.getLock("catalogJson-lock");
        //加锁
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            //业务代码
            dataFromDB = getDataFromDB();
        }finally {
            lock.unlock();
        }
        return dataFromDB;
    }

    /**
     * 使用分布式锁
     * 从数据库查询并封装分类数据
     */
    //
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //1 抢占分布式锁 去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功......");
            //加锁成功 占到了坑位 ---> 执行业务
            //2 设置过期时间  -- 30s 必须和加锁是同步的 原子的
//            redisTemplate.expire("lock", 30, TimeUnit.MINUTES);
            Map<String, List<Catelog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();//业务代码
            }finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1]  then return redis.call('del',KEYS[1]) else return 0 end";
                //删除锁
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            //获取对比值 + 对比成功删除 = 原子操作  lua脚本解锁
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                //删除我自己的锁
//                redisTemplate.delete("lock");//删除锁
//            }

            return dataFromDB;
        }else {
            //加锁失败 ----> 重试synchronized()
            //次数多，就让其休眠
            System.out.println("获取分布式锁不成功......等待重试");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();//自旋的方式
        }
    }

    /**
     * 使用本地锁
     * 从数据库查询并封装分类数据
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        //加锁 只要是同一把锁，就能锁住，需要这一把锁的所有线程
        //synchronized (this) {springBoot所有的组件，在容器中都是单例的。
        //TODO 本地锁 synchronized JUC(Lock), 在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            //得到锁以后,我们应该再去缓存中确定一次，如果没有才需要继续查询。
            return getDataFromDB();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            //如果缓存不为null，直接可以返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("查询了数据库。。。。。。。");
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //1 查出所有1级分类
        List<CategoryEntity> level1Catrgorys = getParent_cid(selectList, 0L);

        //2 封装分类
        Map<String, List<Catelog2Vo>> parent_cid = level1Catrgorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1 拿到每一个1级分类 查到这个1级分类的2级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //2 封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1 找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catalog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2 封装成指定格式
                            Catelog2Vo.Catalog3Vo catalog3Vo = new Catelog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        //3 查到的数据库再放入缓存， 将对象转为json放在缓存中
        String jsonString = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);//1天过期
        return parent_cid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> {
            return item.getParentCid() == parent_cid;
        }).collect(Collectors.toList());
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    //递归 225
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1 收集当前结点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1 找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2)->{
            //2 菜单 排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}