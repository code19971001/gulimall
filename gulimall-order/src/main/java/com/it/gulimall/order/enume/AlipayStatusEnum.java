package com.it.gulimall.order.enume;

/**
 * @author : code1997
 * @date : 2021/7/26 22:04
 */
public enum AlipayStatusEnum {

    WAIT_BUYER_PAY("0", "WAIT_BUYER_PAY"),
    TRADE_CLOSED("1", "TRADE_CLOSED"),
    TRADE_SUCCESS("2", "TRADE_SUCCESS"),
    TRADE_FINISHED("3", "TRADE_FINISHED");

    AlipayStatusEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private String code;
    private String msg;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
