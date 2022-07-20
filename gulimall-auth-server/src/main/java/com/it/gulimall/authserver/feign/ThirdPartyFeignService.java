package com.it.gulimall.authserver.feign;

import com.it.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author : code1997
 * @date : 2021/6/23 22:14
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("thirdparty/sms/sendcode")
    R sendCode(@RequestParam("telNumber") String telNumber, @RequestParam("code") String code);

}
