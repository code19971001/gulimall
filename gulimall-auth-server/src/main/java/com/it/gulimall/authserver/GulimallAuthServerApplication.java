package com.it.gulimall.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * spring-session的核心原理：
 * 1）EnableRedisHttpSession
 *    导入了@Import({RedisHttpSessionConfiguration.class})
 *       创建了组件@Bean public RedisOperationsSessionRepository sessionRepository()操作redis实现增删改查
 *       观察：SpringHttpSessionConfiguration->SessionRepositoryFilter::doFilterInternal：防止filter进行请求过滤，每个请求过来都必须经过filter。
 *  2）而且实现了自动延期
 */
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
