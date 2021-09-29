package cn.edu.zjut.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 自定义 rabbitmq 序列化规则（JSON）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    @PostConstruct
    public void initRabbitTemplate() {
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // log.info("confirm callback...CorrelationData: {}, ack: {}, cause: {}", correlationData, ack, cause);
        });

        this.rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // 修改数据库当前消息的状态为错误
            log.info("return callback...message: {}, replyCode: {}, exchange: {}, replyText: {}, routingKey:{}",
                    message, replyCode, exchange, replyText, routingKey);
        });
    }


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
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }


    /**
     * 延迟队列
     */
    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> args = new HashMap<>();
        // 死信交换机
        args.put("x-dead-letter-exchange", "order-event-exchange");
        // 死信路由键
        args.put("x-dead-letter-routing-key", "order.release");
        // 消息过期时间 30s
        args.put("x-message-ttl", 30000);
        /*
         * Queue(String name,  队列名字
         * boolean durable,    是否持久化
         * boolean exclusive,  是否排他（允许多客户端连接）
         * boolean autoDelete, 是否自动删除
         * Map<String, Object> arguments) 属性
         */
        return new Queue("order.delay.queue", true, false, false, args);
    }


    /**
     * 普通队列
     */
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.queue", true, false, false);
    }


    /**
     * 创建订单的 binding
     */
    @Bean
    public Binding orderCreateBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create",
                null);
    }


    @Bean
    public Binding orderReleaseBinding() {
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release",
                null);
    }


    /**
     * 商品秒杀队列
     */
    // @Bean
    // public Queue orderSecKillOrrderQueue() {
    //     Queue queue = new Queue("order.seckill.order.queue", true, false, false);
    //     return queue;
    // }
    //
    // @Bean
    // public Binding orderSecKillOrrderQueueBinding() {
    //     //String destination, DestinationType destinationType, String exchange, String routingKey,
    //     // 			Map<String, Object> arguments
    //     Binding binding = new Binding(
    //             "order.seckill.order.queue",
    //             Binding.DestinationType.QUEUE,
    //             "order-event-exchange",
    //             "order.seckill.order",
    //             null);
    //
    //     return binding;
    // }
}
