package com.it.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.it.common.constant.AuthServerConstant;
import com.it.common.utils.HttpUtils;
import com.it.common.utils.R;
import com.it.gulimall.authserver.feign.MemberFeignService;
import com.it.gulimall.authserver.vo.GiteeUser;
import com.it.common.vo.MemberResponseVo;
import com.it.gulimall.authserver.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * 处理社交登陆请求
 *
 * @author : code1997
 * @date : 2021/6/28 23:13
 */
@Controller
public class OAuth2Controller {

    private static final String LOGIN_PAGE = "http://auth.gulimall.com/login.html";

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 社交登陆成功的回调
     * 1) 分布式session共享的问题:spring-session
     * 2）默认的作用域是当前域，无法解决子域之间的session共享问题
     * 3）存储到redis中的时候，默认使用jdk的序列化机制，我们可以使用json的序列化方式来序列化对象到json
     */
    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session , HttpServletResponse servletResponse) throws Exception {
        //根据code换取accessToken
        HashMap<String, String> params = new HashMap<>();
        params.put("client_id", "72fcf38e996408f8e8ff2a2f1d355652379cc49ee4d899bf8e0fbf2c8d6024f8");
        params.put("client_secret", "11c9f2f7703b83d9b2d6320fdff3d0072b7456d76cdf76566400ea697263f172");
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "POST", new HashMap<>(), null, params);
        //根据access_token获取用户信息
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取到了access_token
            ////如果当前用户是第一次进入网站，自动注册进来，为当前社交用户生成一个会员账户信息。
            SocialUser socialUser = JSON.parseObject(EntityUtils.toString(response.getEntity()), SocialUser.class);
            HashMap<String, String> userParams = new HashMap<>();
            userParams.put("access_token", socialUser.getAccess_token());
            HttpResponse userResponse = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", new HashMap<>(), userParams);
            if (userResponse.getStatusLine().getStatusCode() == 200) {
                GiteeUser giteeUser = JSON.parseObject(EntityUtils.toString(userResponse.getEntity()), GiteeUser.class);
                giteeUser.setAccess_token(socialUser.getAccess_token());
                giteeUser.setExpires_in(socialUser.getExpires_in());
                try {
                    R r = memberFeignService.oauth2Login(giteeUser);
                    if (r.getCode() == 0) {
                        MemberResponseVo member = r.getData("member", new TypeReference<MemberResponseVo>() {
                        });
                        System.out.println("用户登陆成功：用户信息：" + member);
                        session.setAttribute(AuthServerConstant.LOGIN_USER, member);
                        return "redirect:http://gulimall.com/index.html";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "redirect:" + LOGIN_PAGE;
                }
            }
            return "redirect:" + LOGIN_PAGE;
        } else {
            return "redirect:" + LOGIN_PAGE;
        }
    }
}
