package com.it.gulimall.seckill.interceptor;

import com.it.common.constant.AuthServerConstant;
import com.it.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author : code1997
 * @date : 2021/7/13 21:00
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberResponseVo> threadLocal = new ThreadLocal<>();


    /**
     * 远程服务调用也会要求进行登陆，是不合理的，因此可以添加路径进行匹配。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/kill", requestURI);
        if (!match) {
            return true;
        }
        MemberResponseVo loginUser = (MemberResponseVo) request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (loginUser == null) {
            //去登陆
            request.getSession().setAttribute("msg", "需要登陆才可以访问");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
        threadLocal.set(loginUser);
        return true;
    }
}
