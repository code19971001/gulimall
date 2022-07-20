package com.it.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 可以自动注入原则
 * 方式1：ThreadPoolConfigProperties加上注解：@Component
 * 方式2：@EnableConfigurationProperties({com.it.gulimall.product.config.ThreadPoolConfigProperties.class})
 *
 * @author : code1997
 * @date : 2021/6/21 23:32
 */

//@EnableConfigurationProperties({com.it.gulimall.product.config.ThreadPoolConfigProperties.class})
@Configuration
public class MyThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties properties) {
        return new ThreadPoolExecutor(
                properties.getCoreSize(),
                properties.getMaxSize(),
                properties.getKeepAliveTime(),
                properties.getTimeUnit(),
                new LinkedBlockingQueue<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
