package com.it.gulimall.seckill.to;

import com.it.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : code1997
 * @date : 2021/7/28 0:03
 */
@Data
public class SeckillSkuRedisTo {

    private Long id;

    private Long promotionId;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private BigDecimal seckillCount;

    private BigDecimal seckillLimit;

    private Integer seckillSort;

    //sku的详细信息
    private SkuInfoVo skuInfoVo;

    private Long startTime;

    private Long endTime;

    private String randomCode;


}
