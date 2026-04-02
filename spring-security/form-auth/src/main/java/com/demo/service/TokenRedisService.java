package com.demo.service;

import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT Token 存储服务
 * 
 * 职责：
 * 1. 将签发的 JWT Token 存储到 Redis，设置 TTL 为 1 天
 * 2. 从 Redis 中删除 Token（登出时调用）
 * 3. 检查 Token 是否存在于 Redis
 * 
 * Redis Key 命名规范：
 * - `jwt:token:{tokenHash}` — 使用 Token 的 SHA256 hash 作为 key
 * - 支持多设备登录：不同设备可以持有不同的 Token
 * 
 * Token TTL：
 * - 1 天（86400 秒）
 * - 与 JWT Token 自身的过期时间保持一致
 * - Redis 会自动清理过期的 Token
 * 
 * 使用场景：
 * - 登入时：保存 Token 到 Redis
 * - 登出时：删除 Token 从 Redis
 * - 验证时：检查 Token 是否存在（防止已登出的 Token 继续使用）
 */
@Service
public class TokenRedisService {

    private static final String TOKEN_PREFIX = "jwt:token:";
    private static final long TOKEN_EXPIRE_SECONDS = 86400L; // 1 天
    
    private final StringRedisTemplate redisTemplate;
    
    @Value("${jwt.expiration:86400000}")
    private long tokenExpiration;

    public TokenRedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 保存 Token 到 Redis
     * 
     * @param token JWT Token 字符串
     * @return Redis 中的 key
     */
    public String saveToken(String token) {
        // 使用 Token 的 SHA256 hash 作为 Redis key
        // 这样避免了直接存储完整的 Token，更安全
        String tokenHash = DigestUtil.sha256Hex(token);
        String redisKey = TOKEN_PREFIX + tokenHash;
        
        // 计算 Token 的过期时间（秒）
        long expireSeconds = calculateExpireSeconds();
        
        // 存储到 Redis，设置 TTL
        redisTemplate.opsForValue().set(redisKey, token, expireSeconds, TimeUnit.SECONDS);
        
        return redisKey;
    }

    /**
     * 从 Redis 中删除 Token（登出时调用）
     * 
     * @param token JWT Token 字符串
     * @return 删除是否成功
     */
    public boolean deleteToken(String token) {
        String tokenHash = DigestUtil.sha256Hex(token);
        String redisKey = TOKEN_PREFIX + tokenHash;
        return redisTemplate.delete(redisKey);
    }

    /**
     * 检查 Token 是否存在于 Redis
     * 
     * 用途：
     * - 验证 Token 是否已被删除（登出）
     * - 防止使用已登出的 Token 继续访问受保护资源
     * 
     * @param token JWT Token 字符串
     * @return Token 是否存在
     */
    public boolean isTokenExists(String token) {
        String tokenHash = DigestUtil.sha256Hex(token);
        String redisKey = TOKEN_PREFIX + tokenHash;
        return redisTemplate.hasKey(redisKey);
    }

    /**
     * 计算 Token 的过期时间（秒）
     * 
     * 将 JWT 配置中的过期时间（毫秒）转换为秒
     * 如果配置无效，使用默认值 1 天（86400 秒）
     * 
     * @return 过期时间（秒）
     */
    private long calculateExpireSeconds() {
        if (tokenExpiration > 0) {
            return tokenExpiration / 1000;
        }
        return TOKEN_EXPIRE_SECONDS;
    }
}
