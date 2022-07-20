package com.it.gulimall.member.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * FeignClient ： 表明这是一个声明式的远程调用客户端。
 * @author : code1997
 * @date : 2021/1/10 10:50
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();

}