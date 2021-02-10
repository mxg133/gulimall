package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.vo.OrderItemVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
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
     */
//    @Transactional(rollbackFor = NoStockException.class)
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {

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
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, skuWareHsaStock.getNum());
                if (count == 1) {
                    //当前仓库失败锁成功
                    skuStock = true;
                    break;//没必要查别的仓库了
                }else {
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

    @Data
    class SkuWareHsaStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}