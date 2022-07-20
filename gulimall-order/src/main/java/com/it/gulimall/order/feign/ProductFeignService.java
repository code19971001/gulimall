package com.it.gulimall.order.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : code1997
 * @date : 2021/7/16 0:46
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("product/spuinfo/spuInfo/{skuId}")
    R getSpuInfoBySkuId(@PathVariable Long skuId);
}
