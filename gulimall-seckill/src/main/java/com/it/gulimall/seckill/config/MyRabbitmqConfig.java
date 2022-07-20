package com.it.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 容器一启动就自动创建Binding,queue,Exchange.rabbit mq只要已经存在，即使睡醒发生变化，也不会覆盖。
 *
 * @author : code1997
 * @date : 2021/7/13 21:05
 */
@Configuration
public class MyRabbitmqConfig {


    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
