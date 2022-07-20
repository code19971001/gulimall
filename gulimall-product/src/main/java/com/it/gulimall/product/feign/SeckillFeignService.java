package com.it.gulimall.product.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : code1997
 * @date : 2021/7/29 22:37
 */
@FeignClient("gulimall-seckill")
public interface SeckillFeignService {

    @GetMapping("/sku/SeckillInfo/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);

}
