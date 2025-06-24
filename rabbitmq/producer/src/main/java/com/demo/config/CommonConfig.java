package com.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/24 17:16
 */
@Configuration
@Slf4j
public class CommonConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 消息无法被路由到任何队列时的回调
        rabbitTemplate.setReturnsCallback(msg -> {
            log.info("消息发送失败: {}, {}, {}, {}",
                    msg.getMessage(),
                    msg.getReplyCode(),
                    msg.getReplyText(),
                    msg.getExchange() + " - " + msg.getRoutingKey()
            );
        });
    }
}
