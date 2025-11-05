package com.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyBatis-Plus 演示应用启动类
 *
 * 演示功能：
 * 1. 基本的增删改查操作
 * 2. 分页查询
 * 3. 条件查询（LambdaQueryWrapper）
 * 4. 自定义SQL查询（XML方式）
 * 5. 自动填充（创建时间、更新时间）
 * 6. 逻辑删除
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@MapperScan("com.demo.mapper")
@SpringBootApplication
public class StudyMybatisPlus {
    public static void main(String[] args) {
        SpringApplication.run(StudyMybatisPlus.class, args);
    }
}