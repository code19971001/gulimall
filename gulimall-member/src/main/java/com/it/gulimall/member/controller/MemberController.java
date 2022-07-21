package com.it.gulimall.member.controller;

import com.it.common.exception.BizCodeEnume;
import com.it.common.utils.PageUtils;
import com.it.common.utils.R;
import com.it.gulimall.member.entity.MemberEntity;
import com.it.gulimall.member.exeception.PhoneExistException;
import com.it.gulimall.member.exeception.UsernameExistException;
import com.it.gulimall.member.feign.CouponFeignService;
import com.it.gulimall.member.service.MemberService;
import com.it.gulimall.member.vo.GiteeUser;
import com.it.gulimall.member.vo.MemberLoginVo;
import com.it.gulimall.member.vo.MemberRegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;


/**
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 11:12:34
 */
@Slf4j
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @PostMapping("/oauth2/login")
    public R oauth2Login(@RequestBody GiteeUser giteeUser) {
        System.out.println(giteeUser);
        MemberEntity memberEntity = memberService.socialLogin(giteeUser);
        if (memberEntity !=null ) {
            return R.ok().put("member", memberEntity);
        }else {
            return R.error(BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo loginVo) {
        MemberEntity memberEntity = memberService.login(loginVo);
        if (memberEntity != null) {
            return R.ok().put("member", memberEntity);
        } else {
            return R.error(BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnume.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }

    }

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R r = couponFeignService.memberCoupons();
        Object coupons = r.get("coupons");
        return Objects.requireNonNull(R.ok().put("member", memberEntity)).put("coupons", coupons);

    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo memberRegistVo) {
        try {
            System.out.println("MemberController==>");
            memberService.regist(memberRegistVo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(), BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
