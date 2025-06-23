package com.demo;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

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
    public void testSendMessageToQueue() {
        String message = "Hello, RabbitMQ!";
        String queueName = "simple.queue";
        for (int i = 0; i < 50; ++i) {
            rabbitTemplate.convertAndSend(queueName,message + i);
        }
    }

    @Test
    public void testSendMessageToFanoutExchange() {
        String message = "Hello, RabbitMQ!";
        String exchangeName = "rabbitmq.fanout";
        String routingKey = ""; // 广播类型交换机的路由键可以为空
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }

    @Test
    public void testSendMessageToDirectExchange() {
        String message = "Hello, RabbitMQ!";
        String exchangeName = "rabbitmq.direct";
        String routingKey = "blue"; // 只有routeKey为"blue"的队列会接收到消息
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }

    @Test
    public void testSendMessageToTopicExchange() {
        String exchangeName = "rabbitmq.topic";
        String routingKey = "china.news";
        String message = "Hello, RabbitMQ!";
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);

        String routingKey0 = "china.sports";
        String message0 = "China sports news!";
        rabbitTemplate.convertAndSend(exchangeName, routingKey0, message0);
    }

    @Test
    public void testSendObjectToQueue() {
        String queueName = "object.queue";
        Map<String, Object> person = Map.of(
                "name", "Tomatos",
                "age", 18
        );
        rabbitTemplate.convertAndSend(queueName, person);
    }
}
