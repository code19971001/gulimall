package com.it.common.exception;

/**
 * 采用5位状态码进行定义：两位模块定义+三位异常状态码
 * 10：通用
 * 001：参数格式校验
 * 002：短信验证码频率太高
 * 11：商品
 * 12：订单
 * 13：购物车
 * 14：物流
 * 15：用户
 * 21：库存服务
 *
 * @author : code1997
 * @date : 2021/5/3 21:39
 */
public enum BizCodeEnume {

    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_EXCEPTION(10001, "参数校验失败"),
    SMS_CODE_EXCEPTION(10002, "短信验证码获取频率太高,请稍后再试"),
    TO_MANY_REQUEST(10003, "请求流量过大"),
    PRODUCT_SAVE(11000, "商品上架错误"),
    USER_EXIST_EXCEPTION(15001, "用户已存在"),
    PHONE_EXIST_EXCEPTION(15002, "用户已存在"),
    LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION(15003, "账号或密码错误"),
    NO_STOCK_EXCEPTION(21000, "商品的库存不足");


    private int code;
    private String msg;

    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
