package com.demo.entity;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/22 21:08
 */

@Component
public class Consumer {
    // 如果队列之中有消息, 会自动触发该方法
    @RabbitListener(queues = "simple.queue")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
}
