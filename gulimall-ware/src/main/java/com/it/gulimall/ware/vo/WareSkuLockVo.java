package com.it.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * 库存锁定相关的vo
 *
 * @author : code1997
 * @date : 2021/7/19 20:44
 */
@Data
public class WareSkuLockVo {

    /**
     * 为哪一个订单号锁住库存
     */
    private String orderSn;

    /**
     * 所有需要锁住的库存信息
     */
    private List<OrderItemVo> orderItems;

}
