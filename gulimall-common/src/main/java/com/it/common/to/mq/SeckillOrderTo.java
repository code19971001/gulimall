package com.it.common.to.mq;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author : code1997
 * @date : 2021/8/3 23:29
 */
@Data
public class SeckillOrderTo implements Serializable {

    private String orderSn;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private BigDecimal num;

    private Long memberId;

}
