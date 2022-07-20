package com.it.gulimall.seckill.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author : code1997
 * @date : 2021/7/27 22:59
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {


    @GetMapping("coupon/seckillsession/latest3DaySession")
    R getLatest3DaySession();


}
