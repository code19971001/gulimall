package com.it.common.enume;

/**
 * @author : code1997
 * @date : 2021/7/19 20:12
 */
public enum OrderStatusEnum {

    CREATE_NEW(0, "待付款"),
    PAYED(1, "已付款"),
    SENDED(2, "已发货"),
    RECIEVED(3, "已完成"),
    CANCLED(4, "已取消"),
    SERVICING(5, "售后中"),
    SERVICED(6, "售后完成");

    OrderStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private int code;
    private String msg;


    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
