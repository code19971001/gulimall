package com.it.gulimall.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.it.common.constant.AuthServerConstant;
import com.it.common.constant.LoggerConstant;
import com.it.common.exception.BizCodeEnume;
import com.it.common.utils.R;
import com.it.common.vo.MemberResponseVo;
import com.it.gulimall.authserver.feign.MemberFeignService;
import com.it.gulimall.authserver.feign.ThirdPartyFeignService;
import com.it.gulimall.authserver.vo.UserLoginVo;
import com.it.gulimall.authserver.vo.UserRegistVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 发送一个请求直接跳转到一个页面，没有其他的业务逻辑，如果这样的需求比较多，会存在很多冗余的空方法，可以使用viewController来进行代替，
 *
 * @author : code1997
 * @date : 2021/6/22 21:26
 */
@Slf4j
@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    private static final String LOGIN_URL = "http://auth.gulimall.com/login.html";

    private static final String REG_URL = "http://auth.gulimall.com/reg.html";

    private static final String INDEX_URL = "http://gulimall.com/";

    private static final String REDIRECT = "redirect:";

    private static final String ERRORS = "errors";

    @GetMapping("/login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null){
            return "login";
        }
        return REDIRECT + INDEX_URL;
    }

    @PostMapping("/login")
    public String login(UserLoginVo loginVo, RedirectAttributes redirectAttributes, HttpSession httpSession) {
        log.info(LoggerConstant.LOGGER_PREFIX + "LoginController：：LoginController");
        R login = memberFeignService.login(loginVo);
        if (login.getCode() == 0) {
            //登陆成功
            MemberResponseVo member = login.getData("member", new TypeReference<MemberResponseVo>() {
            });
            httpSession.setAttribute(AuthServerConstant.LOGIN_USER, member);
            return REDIRECT + INDEX_URL;
        } else {
            //登陆失败
            HashMap<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute(ERRORS, errors);
            return REDIRECT + LOGIN_URL;
        }
    }

    /**
     * todo:需要做接口防刷功能。
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("telNumber") String telNumber) {
        log.info(LoggerConstant.LOGGER_PREFIX + "LoginController:sendCode");
        //redis存放验证码并防止统一个手机号再60s内再次发送验证码。
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + telNumber);
        if (redisCode != null) {
            long redisTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - redisTime < 60000L) {
                //不能再次发送
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        String code = String.valueOf(UUID.randomUUID().hashCode()).substring(1, 7);
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + telNumber, code + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);
        log.info(LoggerConstant.LOGGER_PREFIX + "LoginController:sendSms");
        thirdPartyFeignService.sendCode(telNumber, code);
        return R.ok();
    }

    /**
     * todo:重定向携带数据是使用session，只要跳转到下一个页面取出这个数据后，session里数据就会删掉，单机情况下没有问题，但是集群中就会出现问题。
     * 注册成功回到首页，回到登录页
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult bindingResult, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute(ERRORS, errors);
            //调用远程服务进行注册:路径映射是get请求，但是当前是post请求,所以不能直接使用forward.为了防止表单重复提交，因此使用重定向
            return REDIRECT + REG_URL;
        }
        String code = vo.getCode();
        String redisCodeWithTime = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (redisCodeWithTime == null || (!code.equalsIgnoreCase(redisCodeWithTime.split("_")[0]))) {
            //验证码错误
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute(ERRORS, errors);
            return REDIRECT + REG_URL;
        }
        //验证码通过进行注册:删除验证码<-令牌机制
        stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        //调用远程接口进行会员的注册
        R r = memberFeignService.regist(vo);
        if (r.getCode() == 0) {
            //成功
            return REDIRECT + LOGIN_URL;
        } else {
            //失败
            Map<String, String> errors = new HashMap<>(3);
            errors.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            attributes.addFlashAttribute(ERRORS, errors);
            return REDIRECT + REG_URL;
        }
    }

}
