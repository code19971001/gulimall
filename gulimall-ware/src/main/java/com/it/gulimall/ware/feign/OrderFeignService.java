package com.it.gulimall.ware.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : code1997
 * @date : 2021/7/22 0:11
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("order/order/status/{ordersn}")
    R getOrderStatus(@PathVariable("ordersn") String orderSn);

}
