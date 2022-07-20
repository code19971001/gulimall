package com.it.gulimall.order.listener;

import com.it.common.constant.LoggerConstant;
import com.it.common.to.mq.SeckillOrderTo;
import com.it.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author : code1997
 * @date : 2021/7/22 20:24
 */

@RabbitListener(queues = {"order.seckill.order.queue"})
@Service
@Slf4j
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;

    /**
     *  创建了一个秒杀订单，我们需要将订单接着往下处理
     */
    @RabbitHandler
    public void handleOrderClose(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        try {
            log.info(LoggerConstant.LOGGER_PREFIX + "开始执行创建秒杀订单！");
            orderService.createSeckillOrder(seckillOrderTo);
            //手动调用支付宝的收单操作
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //如果出现异常就让消息重新回队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

}
