package com.it.gulimall.authserver.vo;

import lombok.Data;

/**
 * @author : code1997
 * @date : 2021/6/29 22:20
 */
@Data
public class GiteeUser {
    private String id;
    private String access_token;
    private long expires_in;
    private String login;
    private String name;

}
