package cn.edu.zjut.listener;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitListener {
    /**
     * 用于初次连接 rabbitmq 通过 bean 创建 exchange、queue、binding
     * 如果 broker 中已经存在，不能覆盖，只能再控制台先删除掉
     */
    // @RabbitListener(queues = "queue.java")
    // public void init(Message message) {
    // }


    /**
     * 商品秒杀队列
     */
    @Bean
    public Queue orderSeckillQueue() {
        return new Queue("order.seckill.queue", true, false, false);
    }


    @Bean
    public Binding orderSeckillBinding() {
        return new Binding(
                "order.seckill.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill",
                null);
    }

}
