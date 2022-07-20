package com.it.gulimall.member.exeception;

/**
 * @author : code1997
 * @date : 2021/6/24 21:34
 */
public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("用户名已将存在");
    }
}
