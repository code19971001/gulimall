package com.it.gulimall.member.exeception;

/**
 * @author : code1997
 * @date : 2021/6/24 21:34
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号已经存在");
    }

}
