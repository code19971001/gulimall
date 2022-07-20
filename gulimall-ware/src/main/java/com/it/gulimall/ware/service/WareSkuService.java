package com.it.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.common.to.SkuHasStockVo;
import com.it.common.to.mq.OrderTo;
import com.it.common.to.mq.StockLockedTo;
import com.it.common.utils.PageUtils;
import com.it.gulimall.ware.entity.WareSkuEntity;
import com.it.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:52:47
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    boolean lockStock(WareSkuLockVo lockVo);

    void unlockStock(StockLockedTo stockLockedTo);

    void unlockStock(OrderTo orderTo);

}

