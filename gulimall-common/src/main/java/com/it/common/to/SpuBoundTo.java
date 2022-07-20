package com.it.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : code1997
 * @date : 2021/5/13 23:11
 */
@Data
public class SpuBoundTo {

    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
