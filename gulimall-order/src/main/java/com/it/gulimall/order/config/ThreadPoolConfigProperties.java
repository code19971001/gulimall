package com.it.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author : code1997
 * @date : 2021/6/21 23:38
 */
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {

    private Integer coreSize = 10;

    private Integer maxSize = 30;

    private Integer keepAliveTime = 10;

    private TimeUnit timeUnit = TimeUnit.SECONDS;

}
