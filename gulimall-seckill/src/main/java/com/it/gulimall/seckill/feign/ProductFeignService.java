package com.it.gulimall.seckill.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author : code1997
 * @date : 2021/7/28 0:09
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {


    @RequestMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

}
