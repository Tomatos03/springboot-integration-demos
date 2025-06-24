package com.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/22 20:47
 */

@Slf4j
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
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        // 回调时机: 接受到RabbitMQ服务的返回消息时, 异步触发回调函数
        correlationData.getFuture().whenComplete((res, ex) -> {
            // 如果发送消息时出现异常（如网络故障、RabbitMQ不可用等）
            if (ex != null) {
                log.error("出现异常: {}", ex.getMessage());
            } else if (!res.isAck()) {
                // 回调触发时机：
                // 1. 交换机不存在
                // 2. 交换机配置错误或权限不足
                // 3. Broker 内部错误（如磁盘满、内存溢出）
                // 4. 消息被强制拒绝
                log.error("NACK: {}", res.getReason());
            } else {
                log.info("消息发送成功: ack");
            }
        });
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message, correlationData);
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

    @Test
    public void sendBulkMessagesToQueue() {
        String queueName = "simple.queue";
        Message message = MessageBuilder.withBody("Hello, RabbitMQ!".getBytes())
                                      .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                                      .build();
        /*
         * 持久消息
         *  开启生产者确认时大约耗时: 29s
         *  不开启生产者确认时大约耗时: 10s
         * 不持久化消息
         *  开启生产者确认时大约耗时: 27s
         *  不开启生产者确认时大约耗时: 9s
         */
        for (int i = 0; i < 1_000_000; ++i) {
            rabbitTemplate.convertAndSend(queueName, message);
        }
        log.info("已发送1000000条消息到队列: {}", queueName);
    }
}
