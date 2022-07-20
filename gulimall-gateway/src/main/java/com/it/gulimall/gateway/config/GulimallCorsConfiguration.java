package com.it.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author : code1997
 * @date :2021-02-2021/2/25 20:38
 */
@Configuration
public class GulimallCorsConfiguration {

    /**
     * 配置filter来配置跨域
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");  //
        corsConfiguration.addAllowedMethod("*");  //支持哪些方法来跨域。
        corsConfiguration.addAllowedOrigin("*");  //允许哪些来源的请求跨域。
        corsConfiguration.setAllowCredentials(true);  //快于请求默认不包含cookie，设置为true代表允许
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
