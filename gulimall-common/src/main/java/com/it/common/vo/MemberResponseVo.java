package com.it.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : code1997
 * @date : 2021/6/29 22:59
 */
@Data
public class MemberResponseVo implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     *
     */
    private Long levelId;
    /**
     *
     */
    private String username;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String nickname;
    /**
     *
     */
    private String mobile;
    /**
     *
     */
    private String email;
    /**
     * ͷ
     */
    private String header;
    /**
     *
     */
    private Integer gender;
    /**
     *
     */
    private Date birth;
    /**
     *
     */
    private String city;
    /**
     * ְҵ
     */
    private String job;
    /**
     *
     */
    private String sign;
    /**
     *
     */
    private Integer sourceType;
    /**
     *
     */
    private Integer integration;
    /**
     *
     */
    private Integer growth;
    /**
     *
     */
    private Integer status;
    /**
     * ע
     */
    private Date createTime;

    private String accessToken;

    private String socialUid;

    private Long expiresIn;

}
