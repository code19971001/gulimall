package com.it.gulimall.cart.vo;

import lombok.Data;

/**
 * @author : code1997
 * @date : 2021/7/4 23:12
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private boolean tempUser = false;
}
