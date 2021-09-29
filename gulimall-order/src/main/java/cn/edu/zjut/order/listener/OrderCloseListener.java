package cn.edu.zjut.order.listener;

import cn.edu.zjut.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 接收【订单模块】【延时】关闭订单的消息
 */
@RabbitListener(queues = "order.release.queue")
@Component
@Slf4j
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void handleOrderClose(Long orderId, Message message, Channel channel) throws IOException {
        log.debug("收到【订单模块】【延时】关闭订单的消息...");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            this.orderService.closeOrder(orderId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicReject(deliveryTag, true);
        }
    }

}
