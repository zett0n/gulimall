package cn.edu.zjut.ware.listener;

import cn.edu.zjut.common.dto.OrderDTO;
import cn.edu.zjut.common.dto.mq.StockLockDTO;
import cn.edu.zjut.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 解锁库存场景
 * 1、下单、锁库存成功，订单没有支付被用户手动取消，或者超时系统自动取消
 * 2、下单、锁库存成功，后续程序异常，导致订单回滚
 */
@RabbitListener(queues = "stock.release.queue")
@Component
@Slf4j
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 接收【库存模块】锁库存时【延时】发送的消息
     */
    @RabbitHandler
    public void handleStockLock(StockLockDTO stockLockDTO, Message message, Channel channel) throws IOException {
        log.debug("收到【库存模块】锁库存时【延时】发送的消息...");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            this.wareSkuService.checkBeforeUnlock(stockLockDTO);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicReject(deliveryTag, true);
        }
    }

    /**
     * 接收【订单模块】关单时【立即】发送的消息
     */
    @RabbitHandler
    public void handleStockLock(OrderDTO orderDTO, Message message, Channel channel) throws IOException {
        log.debug("收到【订单模块】关单时【立即】发送的消息...");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            this.wareSkuService.checkBeforeUnlock(orderDTO);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicReject(deliveryTag, true);
        }
    }

}
