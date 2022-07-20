package com.it.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/13 23:38
 */
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
