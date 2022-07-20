package com.it.gulimall.authserver.vo;

import lombok.Data;

/**
 * @author : code1997
 * @date : 2021/6/28 23:35
 */
@Data
public class SocialUser {

    private String id;
    private String access_token;
    private String token_type;
    private long expires_in;
    private String refresh_token;
    private String scope;
    private long created_at;

}
