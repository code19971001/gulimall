package com.it.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : code1997
 * @date : 2021/7/15 22:18
 */
@Data
public class OrderSubmitVo {

    private Long addrId;
    private Integer payType;
    private String orderToken;
    /**
     * 应付价格，可以进行验价，
     */
    private BigDecimal payPrice;

    /**
     * 备注信息
     */
    private String note;

}
