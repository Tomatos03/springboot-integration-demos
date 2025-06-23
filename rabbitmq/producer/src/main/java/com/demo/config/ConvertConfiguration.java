package com.demo.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/23 19:57
 */
@Configuration
public class ConvertConfiguration {
    // 注册了Jackson的转化器后,会替换默认的Java序列化实现
    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
