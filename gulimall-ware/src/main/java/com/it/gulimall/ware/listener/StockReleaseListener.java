package com.it.gulimall.ware.listener;

import com.it.common.to.mq.OrderTo;
import com.it.common.to.mq.StockLockedTo;
import com.it.gulimall.ware.service.WareSkuService;
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
 * @date : 2021/7/22 0:55
 */
@Service
@RabbitListener(queues = "stock.release.stock.queue")
@Slf4j
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;


    /**
     * 库存自动解锁:需要启动手动ack模式
     * 1）订单成功，锁定库存成功，其他服务失败导致需要库存自动解锁。
     * 2）某个商品锁定库存失败，导致库存工作单，库存详情单，锁定库存回滚。
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("自动解锁库存操作");
        try {
            wareSkuService.unlockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //如果出现异常就让消息重新回队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }

    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("收到过期订单，需要进行解锁库存的操作");
        try {
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            //如果出现异常就让消息重新回队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }


}
