package com.it.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 幂等性：接口幂等性就是用户对同一操作发起的一次请求或者多次请求的结果是一致的，不会因为多多次点击而产生副作用，比如支付场景，用户购买产品扣款成功，
 * 但是返回结果的时候网络比较差，此时钱已经扣除每用户再次点击按钮，此时会进行二次扣款，返回结果成功，用户查询余款发现钱扣除的次数为2.
 *
 * 如何实现：
 * 1）令牌机制：服务器给我们发送令牌，我们提交的时候带上令牌，如果令牌一致则可以提交成功。
 *      -->但是令牌什么时候删除?
 *      -->从存储介质中获取令牌，对比令牌，删除令牌的操作需要保证原子性，对于redis来说我们可以使用lua脚本来实现。
 * 2）锁机制：例如数据实现
 *      -->数据库悲观锁：悲观锁一般和事务一起使用，因此可能会导致被锁的时间比较长。
 *      -->数据库乐观锁：使用版本号，主要用来处理读多，写少的情况，
 *      -->业务层次分布式锁：先锁定数据，处理完成之后释放锁，获取到数据的时候必须先获取锁，
 * 3）数据库的唯一约束：
 * 4）防重表。
 *
 * CAP理论：在我们保证可用性和分区容错的情况下，是无法保证强一致性的。
 * Base理论：对cap的补充，既然无法保证强一致性，但是我们可以适当的采用弱一致性，即最终一致性。
 *  基本可用：指的是分布系统出现故障的时候，允许损失部分的可用性，允许损失部分可用性。但是基本可用不等于系统的不可用。
 *      ->响应时间上的损失：由于出现故障，响应时间允许出现短暂的增加。
 *      ->功能上的损失：为了保证系统的稳定性，部分消费者可能被引导一个降级的页面。
 *  软状态：允许系统存在中间状态，不会影响系统整体的可用性，分布式存储中一般一份数据有多个副本，允许不同的副本同步的延时就是软状态的体现。
 *      例如：mysql replication的异步复制。
 *  最终一致性：系统中的所有数据副本经过一定时间后，最终可以达到一致性的状态，最终一致性是弱一致性的一种特殊状态。
 *
 *  分布式事物的解决方案：刚性事务(ACID) and 柔性事务(BASE)
 *  柔性事务允许一定时间内，不同节点的数据不一致，但是要求最终一致。
 *  1）2PC-两阶提交协议
 *    XA协议比较简单，而且一旦商业数据库实现了XA，那么实现起来比较简单是，但是在高并发的情况下并不理想。
 *  2）柔性事务-TCC事务补偿型方案：支持把自定义的分支事务纳入到全局事务的管理中去。
 *    一阶段prepare行为：调用自定义的prepare逻辑。
 *    二阶段commit行为：调用自定义的commit逻辑。
 *    二阶段rollback：调用自定义的rollback逻辑。
 *  3）柔性事务-最大努力通知型事务。
 *    按过滤进行通知，不保证数据一定可以通知成功，但是会提供可查询接口进行核对，主要用在第三方系统通讯时，比如调用微信或者支付宝支付后
 *    的支付结果通知，这个方案也是结合MQ进行实现的，例如通过MQ发送http请求，设置最大的通知次数，达到通知次数之后即不再进行通知。
 *    案例：银行通知，商户通知，支付宝支付成功异步回调。
 *  4）柔性事务-可靠消息+最终一致性方案(异步确保型)
 *    实现：业务处理服务在业务事务提交之前，向实时消息服务请求发送消息，实时消息服务只记录消息数据，而不是真正的发送。业务处理服务在事务提交之后，
 *    向实时消息服务确认发送，只有在的到确认发送指令后，实时消息服务才会真正的发送。
 *
 *  seata
 *  1) 为每一个微服务创建undo_log表
 *  2）下载seata-server服务器
 *  3) 配置registry.conf 主要用于配置seata注册到哪里，本案例中使用nacos。
 *  4) 导入依赖：spring-cloud-starter-alibaba-seata
 *  5）业务方法上使用注解@GlobalTransaction
 *  6) 所有想要用到分布式事务的微服务都应该使用seata datasource来代理数据源。
 *  7) 所有的微服务导入registry.conf和file.conf，注意组的配置
 */
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@MapperScan("com.it.gulimall.order.dao")
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
