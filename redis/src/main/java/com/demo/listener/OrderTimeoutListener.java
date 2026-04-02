package com.demo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Redis 键过期事件监听器
 * 监听 __keyevent@*__:expired 事件
 *
 * <p>使用前提：Redis 需要开启 notify-keyspace-events Ex 配置
 * <pre>
 * redis.conf:
 *   notify-keyspace-events Ex
 * </pre>
 *
 * <p>适用场景：
 * <ul>
 *   <li>订单超时取消（给订单 key 设置过期时间，过期时触发取消逻辑）</li>
 *   <li>优惠券过期处理</li>
 *   <li>Session 过期清理</li>
 * </ul>
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@Component
public class OrderTimeoutListener extends KeyExpirationEventMessageListener {

    public OrderTimeoutListener(RedisMessageListenerContainer redisMessageListenerContainer) {
        super(redisMessageListenerContainer);
    }

    /**
     * 当 Redis 中任意 key 过期时触发
     *
     * @param message 过期的 key（如 "order:timeout:xxx"）
     * @param pattern 匹配的模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Redis key 过期事件: {}", expiredKey);

        // 按 key 前缀处理不同业务
        if (expiredKey.startsWith("order:timeout:")) {
            String orderId = expiredKey.substring("order:timeout:".length());
            handleOrderTimeout(orderId);
        }
    }

    /**
     * 处理订单超时
     */
    private void handleOrderTimeout(String orderId) {
        log.info("订单 {} 超时未支付，自动取消", orderId);
        // 实际业务中：更新订单状态、恢复库存、退款等
    }
}
