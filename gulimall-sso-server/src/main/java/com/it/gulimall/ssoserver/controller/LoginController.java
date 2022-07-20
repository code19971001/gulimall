package com.it.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author : code1997
 * @date : 2021/7/1 22:36
 */
@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token) {
        return stringRedisTemplate.opsForValue().get(token);
    }

    @PostMapping("/doLogin")
    public String login(String username, String password, String url, HttpServletResponse response) {

        System.out.println("用户名：" + username);
        System.out.println("密码：" + password);
        System.out.println("要回到的地址：" + url);
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            //跳回到之前的页面
            String uuid = UUID.randomUUID().toString().replace("-", "");
            stringRedisTemplate.opsForValue().set(uuid, username);
            response.addCookie(new Cookie("sso_token", uuid));
            return "redirect:" + url + "?token=" + uuid;
        }
        return "login";
    }

    @GetMapping({"/login", "/login.html"})
    public String loginPage(@RequestParam("redirect_url") String redirectUrl, Model model, @CookieValue(value = "sso_token", required = false) String ssoToken) {
        if (!StringUtils.isEmpty(ssoToken)) {
            //有人登陆过
            return "redirect:" + redirectUrl + "?token=" + ssoToken;
        }
        System.out.println(redirectUrl);
        model.addAttribute("url", redirectUrl);
        //跳回到之前的页面
        return "login";
    }

}
