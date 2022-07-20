package com.it.common.to;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : code1997
 * @date : 2021/5/20 21:24
 */
@Data
public class SkuHasStockVo implements Serializable {

    private Long skuId;
    private Boolean hasStock;
}
