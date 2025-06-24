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
        Jackson2JsonMessageConverter jjmc = new Jackson2JsonMessageConverter();
        // 设置是否创建消息ID, jackson默认使用Java提供的UUID工具类生成一个ID作为消息ID
        jjmc.setCreateMessageIds(true);
        return jjmc;
    }
}
