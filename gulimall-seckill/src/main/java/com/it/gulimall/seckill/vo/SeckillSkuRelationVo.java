package com.it.gulimall.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀活动商品关联
 *
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 11:07:43
 */
@Data
public class SeckillSkuRelationVo implements Serializable {

    private Long id;

    private Long promotionId;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private BigDecimal seckillCount;

    private BigDecimal seckillLimit;

    private Integer seckillSort;

}
