package com.it.gulimall.order.web;

import com.it.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * @author : code1997
 * @date : 2021/7/13 19:27
 */
@Controller
public class PageController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable("page") String page) {
        return page;
    }

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest() {
        //订单下单成功
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());
        System.out.println("创建订单成功，时间为：" + LocalDateTime.now().toString());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderEntity);
        return "OK";

    }

}
