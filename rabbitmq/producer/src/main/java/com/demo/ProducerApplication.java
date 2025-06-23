package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/22 21:15
 */
// 使用@SpringBootTest注解的测试类依赖于一个SpringBoot启动类
@SpringBootApplication
public class ProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }
}
