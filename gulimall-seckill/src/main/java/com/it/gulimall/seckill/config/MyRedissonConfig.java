package com.it.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : code1997
 * @date : 2021/5/26 20:18
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 创建RedissonClient
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        //如果使用了安全协议，那么就是rediss
        config.useSingleServer().setAddress("redis://192.168.134.151:6379");
        return Redisson.create(config);
    }
}
