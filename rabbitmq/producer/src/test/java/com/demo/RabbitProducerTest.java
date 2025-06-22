package com.demo;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/22 20:47
 */

@SpringBootTest
public class RabbitProducerTest {
    // IDEA 2025.1.2 会报错: 找不到装配的Bean, 但实际上是可以正常找到的
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage() {
        String message = "Hello, RabbitMQ!";
        String queueName = "simple.queue";
        rabbitTemplate.convertAndSend(queueName,message);
    }
}
