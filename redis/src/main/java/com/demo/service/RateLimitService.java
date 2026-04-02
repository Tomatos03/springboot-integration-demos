package com.demo.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 限流防刷服务
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 接口限流（固定窗口） ====================

    /**
     * 固定窗口限流
     *
     * @param key      限流 key（如接口名）
     * @param maxCount 窗口内最大请求数
     * @param windowSeconds 窗口时间（秒）
     * @return true-允许访问 false-被限流
     */
    public boolean tryAccess(String key, int maxCount, long windowSeconds) {
        String redisKey = "ratelimit:api:" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            // 第一次访问，设置过期时间
            redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }
        if (count != null && count <= maxCount) {
            return true;
        }
        log.warn("接口 {} 被限流，当前次数: {}", key, count);
        return false;
    }

    // ==================== 短信验证码发送间隔控制 ====================

    /**
     * 检查短信发送间隔（默认 60 秒）
     *
     * @param phone 手机号
     * @return true-可以发送 false-请稍后重试
     */
    public boolean trySendSms(String phone) {
        String key = "ratelimit:sms:" + phone;
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, 1, 60, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(absent);
    }

    /**
     * 获取短信剩余冷却秒数
     */
    public Long getSmsCooldown(String phone) {
        String key = "ratelimit:sms:" + phone;
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire != null && expire > 0 ? expire : 0L;
    }

    // ==================== IP 限流 ====================

    /**
     * IP 限流
     *
     * @param ip            IP 地址
     * @param maxCount      窗口内最大请求数
     * @param windowSeconds 窗口时间（秒）
     * @return true-允许访问 false-被限流
     */
    public boolean tryIpAccess(String ip, int maxCount, long windowSeconds) {
        String key = "ratelimit:ip:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        if (count != null && count <= maxCount) {
            return true;
        }
        log.warn("IP {} 被限流，当前次数: {}", ip, count);
        return false;
    }
}
