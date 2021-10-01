package cn.edu.zjut.order.listener;

import cn.edu.zjut.common.dto.mq.SeckillOrderDTO;
import cn.edu.zjut.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "order.seckill.queue")
@Component
@Slf4j
public class OrderSeckillListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void handleOrderSeckill(SeckillOrderDTO seckillOrderDTO, Message message, Channel channel) throws IOException {
        log.debug("收到【秒杀模块】【立即】生成订单的消息...");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            this.orderService.createSeckillOrder(seckillOrderDTO);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicReject(deliveryTag, true);
        }
    }
}
