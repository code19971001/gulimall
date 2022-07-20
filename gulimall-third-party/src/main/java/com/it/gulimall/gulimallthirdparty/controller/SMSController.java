package com.it.gulimall.gulimallthirdparty.controller;

import com.it.common.utils.R;
import com.it.gulimall.gulimallthirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : code1997
 * @date : 2021/6/22 23:57
 */
@RestController
@RequestMapping("thirdparty")
public class SMSController {

    @Autowired
    SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用的
     */
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("telNumber") String telNumber, @RequestParam("code") String code){
        smsComponent.sendSms(telNumber,code);
        return R.ok();
    }
}
