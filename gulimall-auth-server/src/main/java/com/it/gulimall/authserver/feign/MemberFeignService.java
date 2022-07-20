package com.it.gulimall.authserver.feign;

import com.it.common.utils.R;
import com.it.gulimall.authserver.vo.GiteeUser;
import com.it.gulimall.authserver.vo.UserLoginVo;
import com.it.gulimall.authserver.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author : code1997
 * @date : 2021/6/24 22:41
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("member/member/regist")
    public R regist(@RequestBody UserRegistVo userRegistVo);

    @PostMapping("member/member/login")
    public R login(@RequestBody UserLoginVo loginVo);

    @PostMapping("member/member/oauth2/login")
    public R oauth2Login(@RequestBody GiteeUser giteeUser);

}
