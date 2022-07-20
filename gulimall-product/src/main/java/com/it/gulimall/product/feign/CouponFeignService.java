package com.it.gulimall.product.feign;

import com.it.common.to.SkuReductionTo;
import com.it.common.to.SpuBoundTo;
import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author : code1997
 * @date : 2021/5/13 23:05
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * Open feign调用过程：
     * 1）将对象转化为json。
     * 2）找到服务"gulimall-coupon"，会给远程的服务/coupon/spubounds/save，中发送请求.将转成的json放到请求体
     * 3）对方服务收到请求，请求体中存在json数据
     * 4）将请求中的json转化为对象，只要属性名称，类型一一对应就可以封装成功->json数据模型是兼容的，双方服务无需使用同一个to。
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);

}
