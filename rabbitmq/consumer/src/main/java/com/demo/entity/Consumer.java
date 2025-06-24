package com.demo.entity;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/22 21:08
 */

@Component
public class Consumer {
    // 只监听指定的队列
    // @RabbitListener(queues = "simple.queue")
    // 监听指定队列,并将队列绑定到指定的交换机
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "simple.queue", durable = "true"),
                    exchange = @Exchange(value = "rabbitmq.fanout", type = ExchangeTypes.FANOUT),
                    key = "routeKey",
                    arguments = @Argument(name = "x-queue-mode", value = "lazy") // 设置队列为延迟模式, 需要RabbitMQ 3.6.0及以上版本
            )
    )
    public void receiveMessage(String message) throws Exception {
        System.out.println("Received message: " + message);
        throw new Exception("模拟异常");
    }

    // 监听的队列存储了Json格式的消息, 消息会被自动转换为Map对象
    @RabbitListener(queues = "object.queue")
    public void receiveJsonMessage(Map<String, Object> message) {
        System.out.printf("Received JSON message: [%s]%n", message);
    }

    /**
     *
     * 多个消费者监听同一个队列时, 无论处理的速度快慢, 都会轮询分发
     */
    @RabbitListener(queues = "work.queue")
    public void receiveMessage0(String message) throws InterruptedException {
        System.out.printf("[0]Received message: [%s]%n", message);
        Thread.sleep(20);
    }

    @RabbitListener(queues = "work.queue")
    public void receiveMessage1(String message) throws InterruptedException {
        System.err.printf("[1]Received message: [%s]%n", message);
        Thread.sleep(200);
    }

    /**
     * 广播模式交换机会发送到所有绑定的队列
     */
    @RabbitListener(queues = "fanout.queue1")
    public void receiveFanoutExchangeMessage0(String message) {
        System.out.printf("Received message [%s]%n", message);
    }

    @RabbitListener(queues = "fanout.queue2")
    public void receiveFanoutExchangeMessage1(String message) {
        System.out.printf("Received message [%s]%n", message);
    }

    /**
     * 直连模式交换机会根据路由键将消息发送到指定的队列, 如果键不匹配,则丢弃该消息
     */
    @RabbitListener(queues = "direct.queue1")
    public void receiveDirectExchangeMessage0(String message) {
        System.out.printf("[0]Received DirectorExchange message [%s]%n", message);
    }

    @RabbitListener(queues = "direct.queue2")
    public void receiveDirectExchangeMessage1(String message) {
        System.out.printf("[1]Received DirectorExchange message [%s]%n", message);
    }

    @RabbitListener(queues = "topic.queue1")
    public void receiveTopicExchangeMessage0(String message) {
        System.out.printf("[0]Received TopicExchange message [%s]%n", message);
    }

    @RabbitListener(queues = "topic.queue2")
    public void receiveTopicExchangeMessage1(String message) {
        System.out.printf("[1]Received TopicExchange message [%s]%n", message);
    }
}
