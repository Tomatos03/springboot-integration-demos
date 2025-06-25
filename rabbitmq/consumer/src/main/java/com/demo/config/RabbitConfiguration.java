package com.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;

/**
 * @Description: TODO
 * @Author: Tomatos
 * @Date: 2025/6/23 14:09
 */
//@Configuration
public class RabbitConfiguration {
    /**
     * 通过配置类的方法配置延迟交换机 <br>
     * 依赖插件: https://github.com/rabbitmq/rabbitmq-delayed-message-exchange<br>
     */
    @Bean
    public Exchange DelayExChange() {
        return ExchangeBuilder.directExchange("dlx.exchange")
                              .delayed() // 设置交换机为延迟交换机, 用于处理延迟消息
                              .durable(true)
                              .build();
    }

    @Bean
    public Queue simpleQueue() {
        return new Queue("simple.queue", true);
//                 或使用提供的Builder类创建一个简单的队列, 多参数的时候适用
//        return QueueBuilder.durable("simple.queue")
//                           .exclusive()
//                           .autoDelete()
//                           .lazy() // 是否开启懒惰队列
//                           .build();
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("rabbitmq.fanout", true, false);
        // 或使用提供的Builder类创建一个fanout类型的交换机
        // return ExchangeBuilder.fanoutExchange("rabbitmq.fanout")
    }

    @Bean
    Binding bindingDirectExchangeToQueue(Queue queue, Exchange exchange) {
        // 非fanout类型的交换机需要routeKey
        return BindingBuilder.bind(queue)
                             .to(exchange)
                             .with("blue") // 对于FanoutExchange, routeKey可以随意设置或不设置
                             .noargs(); // 不额外参数绑定
    }
}
