package com.it.gulimall.member.web;

import com.it.common.utils.R;
import com.it.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : code1997
 * @date : 2021/7/25 23:13
 */
@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    /**
     * 1）获取到支付宝给我们传来的请求数据，需要进行验证签名。
     * 2）异步通知：支付成功之后，会根据传入的notify_url，通过post请求方式，通知到商户系统。
     */
    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") String pageNum, Model model) {
        //查出当前登录的用户的所有订单列表数据
        Map<String, Object> page = new HashMap<>();
        page.put("page", pageNum);
        R pageInfo = orderFeignService.listOrderItem(page);
        System.out.println(pageInfo);
        model.addAttribute("orders", pageInfo);
        return "orderList";
    }

}
