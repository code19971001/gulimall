package com.it.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.gulimall.member.dao.MemberDao;
import com.it.gulimall.member.dao.MemberLevelDao;
import com.it.gulimall.member.entity.MemberEntity;
import com.it.gulimall.member.entity.MemberLevelEntity;
import com.it.gulimall.member.exeception.PhoneExistException;
import com.it.gulimall.member.exeception.UsernameExistException;
import com.it.gulimall.member.service.MemberService;
import com.it.gulimall.member.vo.GiteeUser;
import com.it.gulimall.member.vo.MemberLoginVo;
import com.it.gulimall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo memberRegistVo) {
        MemberEntity memberEntity = new MemberEntity();
        //需要检查用户名和手机号是否唯一
        checkUsernameUnique(memberRegistVo.getUsername());
        memberEntity.setUsername(memberRegistVo.getUsername());
        checkPhoneUnique(memberRegistVo.getPhone());
        memberEntity.setMobile(memberRegistVo.getPhone());
        //密码进行加密存储
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(encoder.encode(memberRegistVo.getPassword()));
        memberEntity.setCreateTime(new Date());
        //设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(memberLevelEntity.getId());
        this.baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer selectCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (selectCount > 0) {
            throw new PhoneExistException();
        }

    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer selectCount = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (selectCount > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo loginVo) {
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginVo.getLoginAccount()).or().eq("mobile", loginVo.getLoginAccount()));
        if (memberEntity == null) {
            return null;
        } else {
            String entityPassword = memberEntity.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(loginVo.getPassword(), entityPassword);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity socialLogin(GiteeUser giteeUser) {
        String id = giteeUser.getId();
        MemberEntity selectOne = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", id));
        if (selectOne != null) {
            //用户已经注册
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setId(selectOne.getId());
            memberEntity.setAccessToken(giteeUser.getAccess_token());
            memberEntity.setExpiresIn(giteeUser.getExpires_in());
            this.baseMapper.updateById(memberEntity);
            selectOne.setAccessToken(giteeUser.getAccess_token());
            selectOne.setExpiresIn(giteeUser.getExpires_in());
            return selectOne;
        } else {
            //用户还没有注册
            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setUsername(giteeUser.getLogin());
            memberEntity.setNickname(giteeUser.getName());
            memberEntity.setCreateTime(new Date());
            memberEntity.setSocialUid(giteeUser.getId());
            memberEntity.setAccessToken(giteeUser.getAccess_token());
            memberEntity.setExpiresIn(giteeUser.getExpires_in());
            this.baseMapper.insert(memberEntity);
            return memberEntity;
        }
    }


}