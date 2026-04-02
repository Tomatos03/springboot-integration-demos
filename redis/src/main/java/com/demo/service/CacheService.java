package com.demo.service;

import com.demo.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存业务示例
 * 演示缓存穿透处理（空值缓存）、先查缓存再查库等模式
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 空值标记，用于缓存穿透防护 */
    private static final String NULL_VALUE = "NULL";

    /** 模拟数据库 */
    private static final Map<Long, User> MOCK_DB = new HashMap<>();

    static {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("tomatos");
        user1.setEmail("tomatos@example.com");
        MOCK_DB.put(1L, user1);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("jack");
        user2.setEmail("jack@example.com");
        MOCK_DB.put(2L, user2);
    }

    /**
     * 查询用户（先查缓存，再查库）
     */
    public Object getUser(Long id) {
        String key = "cache:user:" + id;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("命中缓存, id={}", id);
            if (NULL_VALUE.equals(cached)) {
                return null;
            }
            return cached;
        }

        log.info("未命中缓存，查询数据库, id={}", id);
        User user = MOCK_DB.get(id);
        if (user != null) {
            redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
        }
        return user;
    }

    /**
     * 查询用户（带缓存穿透防护：空值也缓存）
     */
    public Object getUserSafe(Long id) {
        String key = "cache:user:safe:" + id;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("命中缓存, id={}", id);
            if (NULL_VALUE.equals(cached)) {
                return null;
            }
            return cached;
        }

        log.info("未命中缓存，查询数据库, id={}", id);
        User user = MOCK_DB.get(id);
        if (user != null) {
            redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
        } else {
            // 缓存空值，防止缓存穿透，过期时间短
            redisTemplate.opsForValue().set(key, NULL_VALUE, 5, TimeUnit.MINUTES);
        }
        return user;
    }

    /**
     * 更新/删除缓存
     */
    public void updateUser(User user) {
        MOCK_DB.put(user.getId(), user);
        String key = "cache:user:" + user.getId();
        redisTemplate.delete(key);
        log.info("删除缓存, key={}", key);
    }

    /**
     * 获取缓存过期剩余时间（秒）
     */
    public Long getExpire(Long id) {
        String key = "cache:user:" + id;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
