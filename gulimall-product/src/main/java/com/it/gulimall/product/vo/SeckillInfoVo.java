package com.it.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : code1997
 * @date : 2021/7/28 0:03
 */
@Data
public class SeckillInfoVo {

    private Long id;

    private Long promotionId;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private BigDecimal seckillCount;

    private BigDecimal seckillLimit;

    private Integer seckillSort;

    private Long startTime;

    private Long endTime;

    private String randomCode;


}
