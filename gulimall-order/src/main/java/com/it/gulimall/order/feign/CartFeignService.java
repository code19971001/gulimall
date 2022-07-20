package com.it.gulimall.order.feign;

import com.it.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/7/13 23:03
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("cart/curUserCartItems")
    List<OrderItemVo> getCurUserCartItems();

}
