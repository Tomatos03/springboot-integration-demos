package com.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */
@MapperScan("com.demo.mapper") // 提供这个注解之后不需要手动书写@Mapper
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
