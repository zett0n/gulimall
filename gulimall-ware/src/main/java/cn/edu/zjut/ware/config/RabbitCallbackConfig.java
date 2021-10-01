package cn.edu.zjut.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class RabbitCallbackConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    

    @PostConstruct
    public void initRabbitTemplate() {
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // log.info("confirm callback...CorrelationData: {}, ack: {}, cause: {}", correlationData, ack, cause);
        });

        this.rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // TODO 消息可靠性保证：修改数据库当前消息的状态为错误，处理、重发错误消息...
            log.info("return callback...message: {}, replyCode: {}, exchange: {}, replyText: {}, routingKey:{}",
                    message, replyCode, exchange, replyText, routingKey);
        });
    }

}
