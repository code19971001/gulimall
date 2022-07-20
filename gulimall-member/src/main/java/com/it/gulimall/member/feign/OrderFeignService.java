package com.it.gulimall.member.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author : code1997
 * @date : 2021/7/26 11:29
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @PostMapping("order/order/listOrderItem")
    R listOrderItem(@RequestBody Map<String, Object> params);

}
