package com.it.gulimall.seckill.service;

import com.it.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/7/27 22:55
 */
public interface SeckillService {
    void uploadSeckillSku();

    List<SeckillSkuRedisTo> getCurSeckillSkus();

    SeckillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String skillId, Integer num, String code);

}
