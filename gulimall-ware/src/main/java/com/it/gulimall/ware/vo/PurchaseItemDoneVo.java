package com.it.gulimall.ware.vo;

import lombok.Data;

/**
 * @author : code1997
 * @date : 2021/5/16 11:51
 */
@Data
public class PurchaseItemDoneVo {
    private Long itemId;
    private Integer status;
    private String reason;
}
