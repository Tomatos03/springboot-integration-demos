package com.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁手动实现
 * 使用 Redis SET NX EX + Lua 脚本保证原子性
 *
 * <p>手动实现的局限性：
 * <ul>
 *   <li>不可重入：同一线程无法重复获取同一把锁</li>
 *   <li>不可续期：锁到期后业务未执行完会自动释放</li>
 *   <li>无自动续期：需要自己实现 watchdog 机制</li>
 *   <li>不支持公平锁：无法保证获取锁的顺序</li>
 *   <li>主从切换可能丢锁：Redis 主节点宕机后从节点未同步锁数据</li>
 * </ul>
 *
 * <p>推荐使用 Redisson，它解决了上述所有问题。
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;

    /** Lua 脚本：只释放自己的锁（判断 value 相等再删除） */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";

    /**
     * 尝试加锁
     *
     * @param lockKey        锁的 key
     * @param expireSeconds  过期时间（秒）
     * @return 锁标识（加锁成功），null 表示加锁失败
     */
    public String tryLock(String lockKey, long expireSeconds) {
        String lockValue = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent("lock:" + lockKey, lockValue, expireSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.info("手动加锁成功, key={}, value={}", lockKey, lockValue);
            return lockValue;
        }
        log.warn("手动加锁失败, key={}", lockKey);
        return null;
    }

    /**
     * 释放锁（Lua 脚本保证原子性）
     *
     * @param lockKey   锁的 key
     * @param lockValue 加锁时返回的标识
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script,
                Collections.singletonList("lock:" + lockKey), lockValue);
        boolean success = result != null && result > 0;
        log.info("手动释放锁, key={}, result={}", lockKey, success);
        return success;
    }
}
