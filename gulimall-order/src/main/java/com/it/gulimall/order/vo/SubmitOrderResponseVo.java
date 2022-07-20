package com.it.gulimall.order.vo;

import com.it.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author : code1997
 * @date : 2021/7/15 23:07
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity orderEntity;
    private Integer code;

}
