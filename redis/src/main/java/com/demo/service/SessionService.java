package com.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户会话管理
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "session:token:";
    private static final String USER_TOKEN_PREFIX = "session:user:";

    /** Token 过期时间：30 分钟 */
    private static final long TOKEN_EXPIRE_MINUTES = 30;

    /**
     * 用户登录，生成 Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return Token
     */
    public String login(Long userId, String username) {
        String token = UUID.randomUUID().toString().replace("-", "");

        // 存储 token -> 用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, userInfo,
                TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 记录用户当前 token（单设备登录）
        redisTemplate.opsForValue().set(USER_TOKEN_PREFIX + userId, token,
                TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.info("用户 {} 登录成功, token={}", username, token);
        return token;
    }

    /**
     * 校验 Token
     *
     * @return 用户信息，过期返回 null
     */
    public Object verifyToken(String token) {
        return redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
    }

    /**
     * 登出，清除 Token
     */
    public void logout(String token) {
        Object userInfo = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (userInfo instanceof Map) {
            Object userId = ((Map<?, ?>) userInfo).get("userId");
            if (userId != null) {
                redisTemplate.delete(USER_TOKEN_PREFIX + userId);
            }
        }
        redisTemplate.delete(TOKEN_PREFIX + token);
        log.info("用户登出, token={}", token);
    }

    /**
     * 判断用户是否在线
     */
    public boolean isOnline(Long userId) {
        return redisTemplate.hasKey(USER_TOKEN_PREFIX + userId);
    }
}
