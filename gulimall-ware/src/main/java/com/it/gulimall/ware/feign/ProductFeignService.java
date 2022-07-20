package com.it.gulimall.ware.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author : code1997
 * @date : 2021/5/16 23:01
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

}
