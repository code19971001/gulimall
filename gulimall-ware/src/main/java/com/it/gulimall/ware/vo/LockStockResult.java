package com.it.gulimall.ware.vo;

import lombok.Data;

/**
 * @author : code1997
 * @date : 2021/7/19 20:52
 */
@Data
public class LockStockResult {

    private Long skuId;
    private Integer number;
    private Boolean locked;

}
