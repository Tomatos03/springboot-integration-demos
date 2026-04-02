package com.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 延迟任务服务
 * 基于 ZSet 实现延迟队列：以执行时间戳作为 score，轮询取出到期任务
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DelayTaskService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DELAY_QUEUE_KEY = "delay:order:timeout";

    /**
     * 模拟数据库中的订单
     */
    private static final Map<String, Map<String, Object>> ORDER_DB = new HashMap<>();

    /**
     * 创建订单并加入延迟队列
     *
     * @param userId        用户 ID
     * @param amount        订单金额
     * @param timeoutMinutes 超时时间（分钟）
     * @return 订单号
     */
    public String createOrder(Long userId, BigDecimal amount, int timeoutMinutes) {
        String orderId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 模拟存储订单
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", userId);
        order.put("amount", amount);
        order.put("status", "PENDING");
        order.put("createTime", LocalDateTime.now().toString());
        ORDER_DB.put(orderId, order);

        // 加入延迟队列，score 为过期时间戳（秒）
        long expireTime = System.currentTimeMillis() / 1000 + timeoutMinutes * 60;
        redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, orderId, expireTime);

        log.info("订单 {} 创建成功，{} 分钟后超时", orderId, timeoutMinutes);
        return orderId;
    }

    /**
     * 获取到期的订单（需要定时任务或手动触发轮询）
     *
     * @return 到期订单列表
     */
    public Set<Object> getExpiredOrders() {
        long now = System.currentTimeMillis() / 1000;
        // 获取所有 score <= 当前时间的元素
        return redisTemplate.opsForZSet().rangeByScore(DELAY_QUEUE_KEY, 0, now);
    }

    /**
     * 处理超时订单
     */
    public String processExpiredOrders() {
        Set<Object> expiredOrders = getExpiredOrders();
        if (expiredOrders == null || expiredOrders.isEmpty()) {
            return "没有超时订单";
        }

        int count = 0;
        for (Object orderId : expiredOrders) {
            Map<String, Object> order = ORDER_DB.get(orderId.toString());
            if (order != null && "PENDING".equals(order.get("status"))) {
                order.put("status", "CANCELLED");
                // 从延迟队列移除
                redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, orderId);
                log.info("订单 {} 超时自动取消", orderId);
                count++;
            }
        }
        return "处理了 " + count + " 个超时订单";
    }

    /**
     * 支付订单（从延迟队列移除）
     */
    public boolean payOrder(String orderId) {
        Map<String, Object> order = ORDER_DB.get(orderId);
        if (order == null || !"PENDING".equals(order.get("status"))) {
            return false;
        }
        order.put("status", "PAID");
        redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, orderId);
        log.info("订单 {} 支付成功，已从延迟队列移除", orderId);
        return true;
    }

    /**
     * 获取订单信息
     */
    public Map<String, Object> getOrder(String orderId) {
        return ORDER_DB.get(orderId);
    }

    /**
     * 获取延迟队列剩余任务数
     */
    public Long getQueueSize() {
        return redisTemplate.opsForZSet().zCard(DELAY_QUEUE_KEY);
    }
}
