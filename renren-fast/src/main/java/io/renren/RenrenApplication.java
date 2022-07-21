/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 注意将这个服务加入到我们服务注册列表，
 */
@EnableDiscoveryClient
@SpringBootApplication
public class RenrenApplication {

    public static void main(String[] args) {
        SpringApplication.run(RenrenApplication.class, args);
    }

}