package com.it.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.enume.OrderStatusEnum;
import com.it.common.exception.NoStockException;
import com.it.common.to.SkuHasStockVo;
import com.it.common.to.mq.OrderTo;
import com.it.common.to.mq.StockDetailTo;
import com.it.common.to.mq.StockLockedTo;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.common.utils.R;
import com.it.gulimall.ware.dao.WareSkuDao;
import com.it.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.it.gulimall.ware.entity.WareOrderTaskEntity;
import com.it.gulimall.ware.entity.WareSkuEntity;
import com.it.gulimall.ware.feign.OrderFeignService;
import com.it.gulimall.ware.feign.ProductFeignService;
import com.it.gulimall.ware.service.WareOrderTaskDetailService;
import com.it.gulimall.ware.service.WareOrderTaskService;
import com.it.gulimall.ware.service.WareSkuService;
import com.it.gulimall.ware.vo.OrderItemVo;
import com.it.gulimall.ware.vo.OrderVo;
import com.it.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    OrderFeignService orderFeignService;


    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {
        StockDetailTo stockDetail = stockLockedTo.getStockDetail();
        Long detailId = stockDetail.getId();
        //查询数据库关于这个订单的锁定库存信息。
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailId);
        if (detailEntity != null) {
            //证明库存锁定成功了，需要查看订单情况。如果没有这个订单，必须进行解锁，如果有订单则需要查看订单的状态。
            Long id = stockLockedTo.getId();
            WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(id);
            String orderSn = wareOrderTaskEntity.getOrderSn();
            //根据订单号查询订单的状态
            R order = orderFeignService.getOrderStatus(orderSn);
            if (order.getCode() == 0) {
                OrderVo orderVo = order.getData(new TypeReference<OrderVo>() {
                });
                if (orderVo == null || orderVo.getStatus() == OrderStatusEnum.CANCLED.getCode()) {
                    //只有当前库存工作单状态为1的时候，才可以进行解锁
                    if (detailEntity.getLockStatus() == 1) {
                        //订单被取消，可以解锁库存了
                        this.baseMapper.unlockSkuStock(stockDetail.getSkuId(), stockDetail.getWareId(), stockDetail.getSkuNum());
                        //只要库存被解锁，就更改库存工作单的状态。
                        detailEntity.setLockStatus(2);
                        wareOrderTaskDetailService.updateById(detailEntity);
                    }
                }
            } else {
                //如果远程服务失败了，也应该重新解锁。
                throw new RuntimeException("远程服务失败");
            }
        }
    }

    /**
     * 防止订单服务卡顿，导致订单状态新消息一直改不了，库存消息优先到期，查订单状态为新建状态，什么都不做消息被放走，导致卡顿的订单永远无法解锁库存。
     */
    @Transactional
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        //查询最新库存状态，防止重复解锁库存。
        WareOrderTaskEntity orderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskEntityId = orderTaskEntity.getId();
        List<WareOrderTaskDetailEntity> taskDetailEntities = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntityId).eq("lock_status", 1));
        //进行解锁
        for (WareOrderTaskDetailEntity taskDetailEntity : taskDetailEntities) {
            //订单被取消，可以解锁库存了
            this.baseMapper.unlockSkuStock(taskDetailEntity.getSkuId(), taskDetailEntity.getWareId(), taskDetailEntity.getSkuNum());
            //只要库存被解锁，就更改库存工作单的状态。
            taskDetailEntity.setLockStatus(2);
            wareOrderTaskDetailService.updateById(taskDetailEntity);
        }
    }


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //如果还没有库存记录，则是新增
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (wareSkuEntities.isEmpty()) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //todo:远程调用设置sku的名字,远程调用仅仅是为了获取名字，失败了的影响不大，因此无需进行回滚。
            //实现不回滚的方式：1、catch住异常。2、高级部分实现
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> skuEntity = (Map<String, Object>) (info.get("skuInfo"));
                    wareSkuEntity.setSkuName((String) skuEntity.get("skuName"));
                }
            } catch (Exception e) {
                log.error("远程调用[productFeignService]出现了问题：", e);
            }
            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo stockVo = new SkuHasStockVo();
            Long skuStock = this.baseMapper.getSkuStock(skuId);
            stockVo.setHasStock(skuStock != null && skuStock > 0);
            stockVo.setSkuId(skuId);
            return stockVo;
        }).collect(Collectors.toList());
    }


    /**
     * 为某个订单锁定库存的方法，如果出现异常就进行回滚
     * 解锁库存：
     * 1）下订单成功，但是订单过期没有支付，被系统自动取消，用户手动取消，都需要解锁库存。
     * 2）创建订单成功，锁定库存成功，但是其他业务失败，导致订单回滚，之前锁定的库存需要自动解锁。
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public boolean lockStock(WareSkuLockVo lockVo) {
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(lockVo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

        //找到每个商品在那个仓库中存在库存
        List<OrderItemVo> lockedItems = lockVo.getOrderItems();
        List<SkuWareHasStock> skuWareHasStocks = lockedItems.stream().map(item -> {
            Long skuId = item.getSkuId();
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            //拥有该商品的仓库信息
            List<Long> hasStockWareId = this.baseMapper.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setWareId(hasStockWareId);
            skuWareHasStock.setLockCount(item.getCount().longValue());
            return skuWareHasStock;
        }).collect(Collectors.toList());
        //开始进行锁定库存
        for (SkuWareHasStock skuWare : skuWareHasStocks) {
            boolean skuStocked = false;
            Long skuId = skuWare.getSkuId();
            List<Long> wareIds = skuWare.getWareId();
            if (wareIds == null || wareIds.isEmpty()) {
                //说明没有任何仓库存在这个商品的库存,直接不需要继续进行下去了
                throw new NoStockException(skuId);
            }
            //如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送给mq
            //如果某个商品保存失败，前面保存的工作单就回滚了。
            for (Long wareId : wareIds) {
                //返回1代表锁定成功，0代表锁定失败
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, skuWare.getLockCount());
                if (count == 1) {
                    skuStocked = true;
                    //只要我们想要锁定库存，那么我们需要先保存库存工作单，主要是为了追溯库存锁定状态。
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null, skuId, "skuName", skuWare.getLockCount().intValue(), taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(detailEntity);
                    //发送消息给rabbit mq消息发送成功:
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, stockDetailTo);
                    //需要保存详细信息，防止回滚导致信息丢失
                    stockLockedTo.setStockDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                }
            }
            if (!skuStocked) {
                //当前商品没有锁住
                throw new NoStockException(skuId);
            }
        }
        //肯定全部锁定成功
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Long lockCount;
        private List<Long> wareId;
    }

}