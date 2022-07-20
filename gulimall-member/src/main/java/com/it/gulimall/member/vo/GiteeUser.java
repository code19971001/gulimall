package com.it.gulimall.member.vo;

import lombok.Data;

/**
 * @author : code1997
 * @date : 2021/6/29 22:20
 */
@Data
public class GiteeUser {
    private String id;
    private long expires_in;
    private String access_token;
    private String login;
    private String name;


}
