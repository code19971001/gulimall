package com.it.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.it.common.utils.PageUtils;
import com.it.gulimall.member.entity.MemberEntity;
import com.it.gulimall.member.exeception.PhoneExistException;
import com.it.gulimall.member.exeception.UsernameExistException;
import com.it.gulimall.member.vo.GiteeUser;
import com.it.gulimall.member.vo.MemberLoginVo;
import com.it.gulimall.member.vo.MemberRegistVo;

import java.util.Map;

/**
 * 
 *
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 11:12:34
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 会员注册逻辑
     * @param memberRegistVo ：封装会员信息
     */
    void regist(MemberRegistVo memberRegistVo);

    /**
     * 检查手机号的唯一性:返回值是void，使用异常机制进行处理
     * @param phone ：手机号
     */
    void checkPhoneUnique(String phone) throws PhoneExistException;

    /**
     * 检查用户名的唯一性
     * @param username ：用户名
     */
    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo loginVo);

    MemberEntity socialLogin(GiteeUser giteeUser);
}

