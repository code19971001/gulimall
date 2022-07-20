package com.it.gulimall.seckill.scheduled;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * spring默认cron表达式和quartz的区别
 * 1）默认是6位组成，不允许第7位的年。
 * 2）spring中周一到周日是：1~7.
 * 3）生产环境中，定时任务不应该被阻塞，默认情况下是阻塞的。
 *     解决方式：
 *          1）对于业务代码，使用异步编排使用异步线程来执行。
 *          2）配置定时任务线程池：默认情况下线程数为1.设置spring.task.scheduling.pool.size，不一定好用。
 *          3）使用异步任务的方式执行定时任务:@EnableAsync+@Async+TaskExecutionAutoConfiguration
 *
 * @author : code1997
 * @date : 2021/7/27 22:21
 */
@Component
public class HelloSchedule {

    /**
     *
     */
    @Async
    @Scheduled(cron = "0 0 3 * * ?")
    public void hello() {
        System.out.println("hello,现在时间是：" + LocalDateTime.now());
    }

}
