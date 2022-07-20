package com.it.common.to.mq;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author : code1997
 * @date : 2021/7/21 23:31
 */
@Data
public class StockLockedTo implements Serializable {

    /**
     * 库存工作单的id
     */
    private Long id;
    /**
     * 工作单详情的id
     */
    private StockDetailTo stockDetail;


}
