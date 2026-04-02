package com.demo.controller;

import com.demo.service.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁示例接口
 * 同时展示手动实现和 Redisson 两种方式
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@RestController
@RequestMapping("/lock")
@RequiredArgsConstructor
public class LockController {

    private final DistributedLock distributedLock;
    private final RedissonClient redissonClient;

    // ==================== 手动实现 ====================

    /**
     * 手动加锁/解锁（需要自己管理锁标识）
     * POST /lock/manual?lockKey=order:1&expireSeconds=10
     */
    @PostMapping("/manual")
    public Map<String, Object> manualLock(@RequestParam String lockKey,
                                          @RequestParam(defaultValue = "10") long expireSeconds) {
        String lockValue = distributedLock.tryLock(lockKey, expireSeconds);
        if (lockValue == null) {
            return result("加锁失败，资源已被锁定", null);
        }
        try {
            // 模拟业务处理
            log.info("手动锁 - 业务处理中...");
            Thread.sleep(2000);
            return result("手动锁 - 业务处理完成", lockValue);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result("业务处理异常", null);
        } finally {
            distributedLock.unlock(lockKey, lockValue);
        }
    }

    // ==================== Redisson 实现 ====================

    /**
     * Redisson 简单加锁/解锁
     * POST /lock/redisson?lockKey=order:2
     */
    @PostMapping("/redisson")
    public Map<String, Object> redissonLock(@RequestParam String lockKey) {
        RLock lock = redissonClient.getLock("lock:" + lockKey);
        try {
            // 尝试加锁，最多等待 5 秒，锁过期 10 秒
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                return result("Redisson 加锁失败", null);
            }
            // 模拟业务处理
            log.info("Redisson 锁 - 业务处理中...");
            Thread.sleep(2000);
            return result("Redisson 锁 - 业务处理完成", null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result("业务处理异常", null);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 防重复提交示例
     * POST /lock/idempotent?lockKey=user:1:submit
     */
    @PostMapping("/idempotent")
    public Map<String, Object> idempotent(@RequestParam String lockKey) {
        String lockValue = distributedLock.tryLock(lockKey, 5);
        if (lockValue == null) {
            return result("请勿重复提交", null);
        }
        try {
            // 模拟业务处理
            log.info("防重复提交 - 业务处理中...");
            Thread.sleep(1000);
            return result("业务处理完成", null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return result("业务处理异常", null);
        } finally {
            distributedLock.unlock(lockKey, lockValue);
        }
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
