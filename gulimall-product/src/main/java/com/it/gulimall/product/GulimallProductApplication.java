package com.it.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 *1.整合Mybatis-plus
 *  1）导入依赖：
 *  <dependency>
 *       <groupId>com.baomidou</groupId>
 *        <artifactId>mybatis-plus-boot-starter</artifactId>
 *        <version>3.3.1</version>
 *  </dependency>
 *  2）配置
 *      1.配置数据源：驱动+数据源信息
 *      2.mybatis-plus
 *          使用注解：mapperScan
 *          告诉sql映射文件位置
 *  3）使用逻辑删除：打开mybatisPlus文档，配置文档中有写
 *      > 配置全局逻辑删除规则：logic-delete-value,logic-delete-no-value
 *      > 配置逻辑删除的组件(3.1.1以后就不需要配置)
 *      > bean加上逻辑删除注解：tableLogic
 *2.引入alibaba-oss
 * 1）引入对象存储的starter
 *          <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alicloud-oss</artifactId>
 *         </dependency>
 * 2）配置yml中配置：key，endpoint等信息
 * 3）自动注入ossClient，并进行相关的操作。
 *
 * 3.使用JSR303校验，实现java后端的校验。
 *  1）标注model校验注解
 *  2）给需要校验的bean添加校验注解:@Valid
 *  3）添加异常接受对象：BindResult,可以从中得知是否存在异常。
 *  4）分组校验：对于新增，修改的校验规则是不一样的，因此我们可以使用分组校验的功能
 *      > bean上添加校验注解：@NotNull(message = "状态只能是0或者1",groups = {AddGroup.class,UpdateGroup.class})
 *      > 校验注解上标注什么时候需要进行校验:@Validated(AddGroup.class),该注解是spring的注解.
 *      > 如果使用了Validated注解，且指定了校验组，那么必须要指定是属于哪一个组的，如果不指定组别就不会进行校验。如果没有指定校验组，那么没有组别的会生效。
 * 4.统一的异常处理
 *  1）@ControllerAdevice
 *  2) handleException:处理指定的异常。
 * 5.自定义校验：
 *  1）编写一个自定义校验注解。
 *  2）编写一个自定义的校验器。
 *  3）关联自定义的校验器和自定义的校验注解-->一个校验注解可以指定多个校验器，可以适配多个校验器。
 * 6.页面开发
 *   1）thymeleaf-starter:关闭缓存
 *   2）静态资源放到static文件夹下
 *   3）页面放到tempates下，直接可以进行访问。
 *   4）页面修改实时更新:dev-tools
 *     引入dev-tools
 *     ctrl+shift+f9重新编译页面
 * 7.整合redis
 *   1）引入data-starter-redis
 *   2）配置主机和端口号等
 *   3）使用springboot自动配置好的RedisTemplate或StringRedisTemplate来进行使用。
 *   4）排除lettuce-core：存在堆外内存溢出的原因。
 *   5）使用jedis替换，导入jedis的jar包即可。
 * 8.整合redisson
 * 9.使用springCache简化开发
 *   1）导入依赖
 *   2)开启缓存@EnableCaching
 *   3）写配置
 *     ->默认配置
 *        -->RedisCacheConfiguration
 *        -->RedisCahcheManager
 *     ->我们的配置
 *        -->配置使用redis作为缓存：spring.cache.type=redis
 *     ->测试使用缓存
 *        -->
 *
 */
@EnableRedisHttpSession
@EnableCaching
@EnableFeignClients(basePackages = {"com.it.gulimall.product.feign"})
@EnableDiscoveryClient
@MapperScan("com.it.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
