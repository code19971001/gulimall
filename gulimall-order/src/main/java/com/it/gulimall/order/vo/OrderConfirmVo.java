package com.it.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要使用的数据
 * 1）用户的所有收货地址列表
 * 2）所有选中的购物项,也可称为订单项
 * 3)发票信息
 * 4)优惠券信息：简单的使用积分信息来进行代替。
 * 5)订单总额
 * 6）应付的价格
 * orderToken:是用于放重复的令牌：幂等性
 *
 * @author : code1997
 * @date : 2021/7/13 22:24
 */
@ToString
public class OrderConfirmVo {

    @Getter
    @Setter
    List<MemberReceiveAddressVo> address;

    @Getter
    @Setter
    List<OrderItemVo> orderItems;

    @Getter
    @Setter
    Integer integration;

    //todo:幂等性实现防止重复提交
    @Getter
    @Setter
    String orderToken;

    @Getter
    @Setter
    Map<Long,Boolean> skuStocks;

    public Integer getCount() {
        return orderItems == null ? 0 : orderItems.size();
    }

    public BigDecimal getTotal() {
        BigDecimal price = new BigDecimal("0.00");
        //1.计算购物项总价
        if (orderItems != null && !orderItems.isEmpty()) {
            for (OrderItemVo orderItem : orderItems) {
                System.out.println("当前的购物项的金额：" + orderItem.getTotalPrice());
                price = price.add(orderItem.getPrice().multiply(new BigDecimal(orderItem.getCount())));
            }
        }
        return price;
    }

    public BigDecimal getPayPrice() {

        return getTotal();
    }
}
