package com.it.gulimall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * openFeign远程调用：
 *   1）引入：openFeign
 *   2）写一个接口，告诉spring cloud这个接口是远程调用节点。
 *   3）声明接口中的每一个方法都是调用哪个远程服务的那个请求。
 *   4）开启远程调用的功能。
 * md5加密算法：不可逆算法,但是可以被暴力破解。因此不能直接进行密码的加密存储:DigestUtils.md5Hex
 *   1）压缩性：任意长度的数据，算出的md5值长度都是固定的。
 *   2）容易计算：从原始数据计算出md5值很容易。
 *   3）抗修改性：对元数据进行任何改动，哪怕之修改一个字节，所得到的md5值都有很大的区别。
 *   4）强抗碰撞：想要找到两个不同的数据，是他们具有相同的md5值也是非常困难的。
 * md5&盐值加密：Md5Crypt.md5Crypt(需要存储)/BCryptPasswordEncoder
 *   1）通过生成随机数与md5生成字符串进行组合。
 *   2）数据库同时存储md5与salt值，验证正确性时使用salt进行md5即可。
 *
 */
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.it.gulimall.member.feign")
@EnableDiscoveryClient
@MapperScan("com.it.gulimall.member.dao")
@SpringBootApplication
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
