package com.it.gulimall.order.to;

import com.it.gulimall.order.entity.OrderEntity;
import com.it.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author : code1997
 * @date : 2021/7/15 23:33
 */
@Data
public class OrderCreateTo implements Serializable {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    private BigDecimal payPrice;

    private BigDecimal fare;

}
