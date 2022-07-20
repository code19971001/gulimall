package com.it.common.to.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : code1997
 * @date : 2021/7/21 23:40
 */
@Data
public class StockDetailTo implements Serializable {

    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     *
     */
    private Integer skuNum;
    /**
     *
     */
    private Long taskId;

    private Long wareId;

    /**
     * 1-锁定，2-解锁，3-扣减
     */
    private Integer lockStatus;
}
