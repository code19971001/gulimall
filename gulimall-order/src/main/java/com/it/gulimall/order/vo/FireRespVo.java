package com.it.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : code1997
 * @date : 2021/7/14 23:16
 */
@Data
public class FireRespVo {
    private MemberReceiveAddressVo memberReceiveAddress;
    private BigDecimal fire;
}
