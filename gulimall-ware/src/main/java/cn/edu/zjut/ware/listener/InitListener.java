package cn.edu.zjut.ware.listener;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

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
     * topic 交换机
     */
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }


    /**
     * 延迟队列
     */
    @Bean
    public Queue stockDelayQueue() {
        HashMap<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock-event-exchange");
        args.put("x-dead-letter-routing-key", "stock.release");
        // 消息过期时间 40s
        args.put("x-message-ttl", 40000);

        return new Queue("stock.delay.queue", true, false, false, args);
    }


    /**
     * 普通队列，用于解锁库存
     */
    @Bean
    public Queue stockReleaseQueue() {
        return new Queue("stock.release.queue", true, false, false);
    }


    /**
     * 交换机和延迟队列绑定
     */
    @Bean
    public Binding stockLockBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.lock",
                null);
    }


    /**
     * 交换机和普通队列绑定
     */
    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }
    
}
