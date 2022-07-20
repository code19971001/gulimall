package com.it.gulimall.seckill.scheduled;

import com.it.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务
 * 1）秒杀商品的定时上架
 * 幂等性处理
 * 1）分布式部署的情况下定时任务下多机器同时运行，使用redis的分布式锁来解决。
 * 2）业务接口的幂等性处理。
 *
 * @author : code1997
 * @date : 2021/7/27 22:47
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    private final static String UPLOAD_LOCK = "seckill:upload:lock";

    /**
     * 每天晚上3点，上架最近3天需要上架的商品。
     */
    @Scheduled(cron = "* * 3 * * ?")
    public void uploadSeckillLastest3Days() {
        log.info("开始上架商品信息");
        //重复上架，无需处理
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSku();
        } finally {
            log.info("商品上架执行结束，开始释放锁");
            lock.unlock();
        }
    }

}
