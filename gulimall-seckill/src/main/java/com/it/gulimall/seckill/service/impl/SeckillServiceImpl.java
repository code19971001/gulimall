package com.it.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.it.common.to.mq.SeckillOrderTo;
import com.it.common.utils.R;
import com.it.common.vo.MemberResponseVo;
import com.it.gulimall.seckill.feign.CouponFeignService;
import com.it.gulimall.seckill.feign.ProductFeignService;
import com.it.gulimall.seckill.interceptor.LoginInterceptor;
import com.it.gulimall.seckill.service.SeckillService;
import com.it.gulimall.seckill.to.SeckillSkuRedisTo;
import com.it.gulimall.seckill.vo.SeckillSessionsWithSkus;
import com.it.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author : code1997
 * @date : 2021/7/27 22:56
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private static final String SKUKILL_CACHE_PREFIX = "seckill:skus";

    //加上商品随机码
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    /**
     * 需要设置为幂等性接口
     */
    @Override
    public void uploadSeckillSku() {
        //1.找到需要参与秒杀的活动
        R latest3DaySession = couponFeignService.getLatest3DaySession();
        if (latest3DaySession.getCode() == 0) {
            //远程调用成功
            List<SeckillSessionsWithSkus> sessionData = latest3DaySession.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            //将商品数据缓存到redis中
            saveSessionInfo(sessionData);
            saveSessionSkuInfo(sessionData);
        }
    }

    /**
     * 要求：
     * 1）返回值和原函数保持一致
     * 2）方法参数列表要和原函数一致，或者可以额外多一个Throwable类型的参数用于接收对应的异常。
     * 3）要求函数要和原方法初一统一个类中，如果希望使用其他类的函数，则可以使用fallbackClass指定类，对应的函数必须为static函数，否则解析失败
     */
    public List<SeckillSkuRedisTo> blockHandler(BlockException blockException) {
        //可以接收方法参数等，要求返回值和原方法相同。
        log.error("原方法被限制流量了{}", blockException.getMessage());
        return new ArrayList<>();
    }

    /**
     * 要求：
     * 1）返回值和原函数保持一致
     * 2）方法参数列表要和原函数一致，或者可以额外多一个Throwable类型的参数用于接收对应的异常。
     * 3）要求函数要和原方法初一统一个类中，如果希望使用其他类的函数，则可以指定类，对应的函数必须为static函数，否则解析失败
     */
    public List<SeckillSkuRedisTo> fallback(BlockException blockException) {
        //可以接收方法参数等，要求返回值和原方法相同。
        log.error("原方法被限制流量了{}", blockException.getMessage());
        return new ArrayList<>();
    }

    @SentinelResource(value = "getCurSeckillSkus", blockHandler = "blockHandler", fallback = "fallback")
    @Override
    public List<SeckillSkuRedisTo> getCurSeckillSkus() {
        //确定当前时间属于哪一个场次
        long curTime = System.currentTimeMillis();
        Set<String> redisKeys = stringRedisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : redisKeys) {
            String replacedKey = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] timsScope = replacedKey.split("_");
            long startTime = Long.parseLong(timsScope[0]);
            long endTime = Long.parseLong(timsScope[1]);
            if (curTime >= startTime && curTime <= endTime) {
                //获取所有的信息
                List<String> ranges = stringRedisTemplate.opsForList().range(key, Long.MIN_VALUE, Long.MAX_VALUE);
                BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                if (ranges != null) {
                    List<String> skusInfo = hashOps.multiGet(ranges);
                    if (skusInfo != null) {
                        return skusInfo.stream().map(item -> {
                            SeckillSkuRedisTo seckillSkuTo = new SeckillSkuRedisTo();
                            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                            BeanUtils.copyProperties(skuRedisTo, seckillSkuTo);
                            return seckillSkuTo;
                        }).collect(Collectors.toList());
                    }
                }
                break;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        //找到所有参与秒杀的商品的key的信息
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> hashKeys = hashOps.keys();
        String regex = "\\d_" + skuId;
        if (hashKeys != null && hashKeys.size() > 0) {
            for (String hashKey : hashKeys) {
                boolean matches = Pattern.matches(regex, hashKey);
                if (matches) {
                    SeckillSkuRedisTo skuRedisTo = JSON.parseObject(hashOps.get(hashKey), SeckillSkuRedisTo.class);
                    //需要判断当前时间是否是当前时间
                    Long startTime = skuRedisTo.getStartTime();
                    Long endTime = skuRedisTo.getEndTime();
                    long curTime = System.currentTimeMillis();
                    if (curTime < startTime || curTime > endTime) {
                        skuRedisTo.setRandomCode("");
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * 如果秒杀成功就将其订单号返回。
     */
    @Override
    public String kill(String skillId, Integer num, String code) {
        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String skuInfo = hashOps.get(skillId);
        if (StringUtils.isEmpty(skuInfo)) {
            return null;
        } else {
            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(skuInfo, SeckillSkuRedisTo.class);
            //校验秒杀时间
            Long startTime = seckillSkuRedisTo.getStartTime();
            Long endTime = seckillSkuRedisTo.getEndTime();
            long curTime = System.currentTimeMillis();
            if (curTime > endTime || curTime < startTime) {
                return null;
            }
            //校验code码
            String redisKillId = seckillSkuRedisTo.getPromotionSessionId() + "_" + seckillSkuRedisTo.getSkuId();
            if (!seckillSkuRedisTo.getRandomCode().equalsIgnoreCase(code) || !redisKillId.equalsIgnoreCase(skillId)) {
                return null;
            }
            //校验秒杀的数量
            if (num > seckillSkuRedisTo.getSeckillLimit().intValue()) {
                return null;
            }
            //校验该用户是否已经买过，如果买过就不可以再次进行购买。对redis进行占位操作
            MemberResponseVo memberResponseVo = LoginInterceptor.threadLocal.get();
            String redisKey = memberResponseVo.getId() + "_" + redisKillId;
            //自动过期
            Boolean absent = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), (endTime - startTime), TimeUnit.MILLISECONDS);
            if (!absent.booleanValue()) {
                return null;
            }
            //进行占位
            //需要查看信号量
            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + seckillSkuRedisTo.getRandomCode());
            //不能使用acquire，因为acquire是阻塞式的
            try {
                semaphore.tryAcquire(num, 100L, TimeUnit.MILLISECONDS);
                //秒杀成功，快速下单。
                String orderSn = IdWorker.getTimeId();
                SeckillOrderTo orderTo = new SeckillOrderTo();
                orderTo.setOrderSn(orderSn);
                orderTo.setNum(new BigDecimal(num));
                orderTo.setMemberId(memberResponseVo.getId());
                orderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
                orderTo.setSkuId(seckillSkuRedisTo.getSkuId());
                orderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                //将订单号信息存放到MQ中去
                return orderSn;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }

        }
    }

    private void saveSessionInfo(List<SeckillSessionsWithSkus> sessionData) {
        sessionData.forEach(session -> {
            if (session.getRelationSkus() != null && !session.getRelationSkus().isEmpty()) {
                long startTime = session.getStartTime().getTime();
                long endTime = session.getEndTime().getTime();
                String redisKey = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
                //注意保存商品信息
                List<String> skuIds = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId()).collect(Collectors.toList());
                Boolean hasKey = stringRedisTemplate.hasKey(redisKey);
                if (hasKey == null || !hasKey) {
                    stringRedisTemplate.opsForList().leftPushAll(redisKey, skuIds);
                }
            }
        });
    }

    /**
     * todo:bug need to fix 场次已经存在是无法覆盖的,按理应该是更新操作。
     */
    private void saveSessionSkuInfo(List<SeckillSessionsWithSkus> sessionData) {
        BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        sessionData.forEach(session -> session.getRelationSkus().forEach(skuItem -> {
            Boolean redisSku = hashOps.hasKey(skuItem.getPromotionSessionId() + "_" + skuItem.getSkuId());
            if (redisSku == null || !redisSku) {
                SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                //sku的基本信息
                BeanUtils.copyProperties(skuItem, redisTo);
                //sku的详细信息
                R skuInfo = productFeignService.info(skuItem.getSkuId());
                if (skuInfo.getCode() == 0) {
                    SkuInfoVo skuData = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                    });
                    redisTo.setSkuInfoVo(skuData);
                }
                //配置当前商品的秒杀时间信息
                long startTime = session.getStartTime().getTime();
                long endTime = session.getEndTime().getTime();
                redisTo.setStartTime(startTime);
                redisTo.setEndTime(endTime);
                //设置秒杀码？只直到skuId是不可以的，必须带上秒杀码才可以
                String randomCode = UUID.randomUUID().toString().replace("-", "");
                //因为是秒杀系统，因此不可能直接访问库存系统数据库，因此我们可以使用分布式信号量来代替库存,注：不同场次之间的sku互不影响。
                RSemaphore skuStockSemaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                skuStockSemaphore.trySetPermits(skuItem.getSeckillCount().intValue());
                redisTo.setRandomCode(randomCode);
                //保存秒杀商品的信息到redis中去
                hashOps.put(skuItem.getPromotionSessionId() + "_" + skuItem.getSkuId(), JSON.toJSONString(redisTo));
            }
        }));
    }

}
