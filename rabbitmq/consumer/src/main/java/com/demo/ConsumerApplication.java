package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/22 21:05
 */

// RabbitConfiguration类没有被使用, 因为Consumer类已经使用注解方式创建了队列和交换机
// 如果需要以配置类的方式创建队列和交换机, 可以从参考RabbitConfiguration类
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
