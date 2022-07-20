package com.it.gulimall.cart.config;

import com.it.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author : code1997
 * @date : 2021/7/4 23:38
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
