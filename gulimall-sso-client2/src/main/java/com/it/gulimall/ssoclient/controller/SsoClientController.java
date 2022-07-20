package com.it.gulimall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

/**
 * @author : code1997
 * @date : 2021/7/1 22:23
 */
@Controller
public class SsoClientController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Value("${sso.server.url}")
    String ssoServerUrl;

    RestTemplate restTemplate = new RestTemplate();

    /**
     * 无需登陆就可以登陆
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {

        return "hello";
    }

    /**
     * 需登陆才可以访问
     */
    @GetMapping("/boss")
    public String employees(Model model, HttpSession session, @RequestParam(value = "token", required = false) String token) {
        if (token == null && session.getAttribute("loginUser") == null) {
            System.out.println("跳转到登陆服务器进行登陆");
            System.out.println(ssoServerUrl);
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        } else {

            String object = restTemplate.getForObject("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            session.setAttribute("username", object);
            ArrayList<String> emps = new ArrayList<>();
            emps.add("张三");
            emps.add("李四");
            model.addAttribute("emps", emps);
            return "list";
        }
    }
}
