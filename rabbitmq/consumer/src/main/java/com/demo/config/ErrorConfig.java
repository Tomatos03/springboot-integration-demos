package com.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/24 22:35
 */
@Configuration
@ConditionalOnProperty(
        name = "spring.rabbitmq.listener.simple.retry.enabled",
        havingValue = "true"
)
public class ErrorConfig {
    @Bean
    public Queue errorQueue() {
        // 创建一个名为"error.queue"的队列, 用于存储处理失败的消息
        return new Queue("error.queue", true);
    }

    @Bean
    public Exchange errorExchange() {
        return ExchangeBuilder.directExchange("error.exchange")
                .durable(true)
                .build();
    }

    @Bean
    public Binding bindingErrorQueueToExchange(Queue errorQueue, Exchange errorExchange) {
        // 将错误队列绑定到错误交换机
        return BindingBuilder.bind(errorQueue)
                             .to(errorExchange)
                             .with("error.msg")
                             .noargs();
    }

    @Bean
    MessageRecoverer errorMessageRecoverer(RabbitTemplate rabbitTemplate,
                                            Exchange errorExchange) {
        // MessageRecoverer用于配置失败重试时的恢复机制, 有多种实现类可选:：
        // 1. RejectAndDontRequeueRecoverer(默认实现)：消息消费失败时直接拒绝且不重回队列，消息会被丢弃
        // 2. ImmediateRequeueMessageRecoverer：消息消费失败时立即重回队列，适合临时性错误重试。
        // 3. RepublishMessageRecoverer：消息消费失败时将消息重新发布到指定的exchange便于后续分析和处理。
        return new RepublishMessageRecoverer(rabbitTemplate, errorExchange.getName(), "error.msg");
    }
}
