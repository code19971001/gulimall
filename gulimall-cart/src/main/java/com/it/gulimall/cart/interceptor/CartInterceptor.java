package com.it.gulimall.cart.interceptor;

import com.it.common.constant.AuthServerConstant;
import com.it.common.constant.CartConstant;
import com.it.common.vo.MemberResponseVo;
import com.it.gulimall.cart.vo.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 执行目标方法之前，判断用户的登陆状态，并封装传递给controller目标请求
 * 需要配置：WebMvcConfigurer::addInterceptors()
 *
 * @author : code1997
 * @date : 2021/7/4 23:06
 */
public class CartInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberResponseVo loginUser = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginUser != null) {
            //用户登陆了
            userInfoTo.setUserId(loginUser.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                }
            }
        }
        //如果没有临时用户一定需要分配一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            userInfoTo.setUserKey(UUID.randomUUID().toString());
        }
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     * 业务执行之后，让浏览器保存一个cookie，并保存一个月
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.isTempUser()) {
            //持续延长临时用户的过期时间
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_EXPIRES);
            response.addCookie(cookie);
        }
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
