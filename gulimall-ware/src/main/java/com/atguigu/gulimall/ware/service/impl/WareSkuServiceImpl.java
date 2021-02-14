package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.StockDetailTo;
import com.atguigu.common.to.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderFeignService orderFeignService;

    @Override
    // wareId: 123,//仓库id
    // skuId: 123//商品id
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params), wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
            //1、自己catch异常
            //TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wareSkuDao.insert(skuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map((skuId) -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //select sum(stock-stock_locked) from wms_ware_sku where sku_id=1
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            //查询当前sku的总库存量
            return vo;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * 为某个订单锁定库存
     * rollbackFor 代表这是一定要回滚的
     * RuntimeException 也是回滚的。
     * <p>
     * 库存解锁的场景
     * 1 下订单成功，订单过期，没有支付被系统自动取消/被用户手动取消
     * 2 下订单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁
     */
//    @Transactional(rollbackFor = NoStockException.class)
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

        //保存库存工作单的详情
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        //1 按照下单的收货地址，找到一个最近的仓库，锁定库存。
        //1 找到每个商品在哪个仓库都有库存
        List<OrderItemVo> orderItemVos = vo.getLocks();
        List<SkuWareHsaStock> SkuWareHsaStocks = orderItemVos.stream().map((item) -> {
            SkuWareHsaStock skuWareHsaStock = new SkuWareHsaStock();
            //找到了具体的购物项
            Long skuId = item.getSkuId();
            //扣库存
            skuWareHsaStock.setSkuId(skuId);
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            skuWareHsaStock.setWareId(wareIds);
            skuWareHsaStock.setNum(item.getCount());
            return skuWareHsaStock;
        }).collect(Collectors.toList());
        //2 锁库存
        for (SkuWareHsaStock skuWareHsaStock : SkuWareHsaStocks) {
            Boolean skuStock = false;
            Long skuId = skuWareHsaStock.getSkuId();
            List<Long> wareIds = skuWareHsaStock.getWareId();
            if (wareIds == null && wareIds.size() == 0) {
                //没有任何仓库有库存
                throw new NoStockException(skuId);
            }
            //有库存
            //1 如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
            //2 如果失败 前面保存的工作单的信息就回滚了。发送出去的消息，即使要解锁记录，由于去数据库查不到ID，所以不用解锁
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, skuWareHsaStock.getNum());
                if (count == 1) {
                    //当前仓库失败锁成功
                    skuStock = true;
                    //TODO 告诉MQ锁定成功 发消息
                    //保存锁成功了的详情
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", skuWareHsaStock.getNum(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockDetailTo);
                    //只发id不行，防止回滚以后找不到数据
                    stockLockedTo.setDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;//没必要查别的仓库了
                } else {
                    //当前仓库失败锁失败
                }
            }
            if (skuStock == false) {
                //当前商品所有仓库都没有锁住
                throw new NoStockException(skuId);
            }
        }
        //3 代码能够到这里，全部商品锁定成功
        return true;
    }

    @Override
    public void unLockStock(StockLockedTo to) {

        StockDetailTo detailTo = to.getDetailTo();
        Long detailId = detailTo.getId();
        //查询数据库的锁库存的消息
        //有 证明 库存锁定OK
        //没有。库存锁定失败,库存回滚 无需解锁
        WareOrderTaskDetailEntity orderTaskDetailEntity = wareOrderTaskDetailService.getById(detailId);
        if (orderTaskDetailEntity != null) {
            //解锁 库存没毛病
            //库存工作单的id
            Long id = to.getId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn();
            //查询订单状态 订单应该是取消的状态
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() == 0) {
                //ok
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                });
                if (orderVo == null || orderVo.getStatus() == 4) {
                    //订单不存在||订单取消 解锁库存
                    if (orderTaskDetailEntity.getLockStatus() == 1) {
                        //当前工作单详情，状态为1(已锁定)
                        unLockStock(detailTo.getSkuId(), detailTo.getWareId(), detailTo.getSkuNum(), detailId);
                    }
                }
            } else {
                //消息拒绝重新放入队列，让别人继续消费解锁
                throw new RuntimeException("远程服务失败");
            }
        } else {
            //无需解锁 库存服务自己出现问题
        }
    }

    /**
     * 防止订单服务卡顿，导致库存订单状态一直改变不了
     * 库存消息优先到期查订单状态，新建状态什么都不做就走了
     * 导致卡顿的订单，永远不能解锁库存
     */
    @Transactional
    @Override
    public void unLockStock(OrderTo orderTo) {

        String orderSn = orderTo.getOrderSn();
        //查一下最新的状态，防止重复解锁库存
        WareOrderTaskEntity wareOrderTask = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long id = wareOrderTask.getId();
        //按照工作单，找到所有没有解锁的库存 进行解锁
        List<WareOrderTaskDetailEntity> entities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", id).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getTaskId());
        }
    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {

        //库存恢复
        wareSkuDao.unLockStock(skuId, wareId, num);
        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);//变为已解锁
        wareOrderTaskDetailService.updateById(entity);
    }

    @Data
    class SkuWareHsaStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}