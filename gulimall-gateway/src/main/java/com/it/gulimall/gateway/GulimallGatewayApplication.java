package com.it.gulimall.gateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 需要将自己注册到配置中心。
 * 1.开启服务的注册和发现
 *  nacos-server
 *
 * 2.common中存在mybatis相关的操作，因此我们可以选择进行配置数据源或者排除数据源,对于gateway来说是不需要进行数据库的配置，因此可以排除该设置。
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallGatewayApplication.class, args);
    }

}
