package org.demo.manager.jwt;

import io.jsonwebtoken.Claims;
import lombok.NonNull;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * 已完成签名校验和解析的 JWT 只读对象。
 *
 * @author : Tomatos
 * @date : 2026/3/31
 */
public final class ParsedToken {

    private final Claims claims;

    /**
     * 包级构造器，只允许同包的 JWTManager 创建，避免外部包装未经校验的 Claims。
     */
    ParsedToken(@NonNull Claims claims) {
        this.claims = claims;
    }

    /**
     * 泛型读取声明，返回类型由调用方指定。
     *
     * @param key 声明名称
     * @param <T> 声明值类型
     * @return 声明值；不存在时返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull String key) {
        return (T) claims.get(key);
    }

    public String getString(@NonNull String key) {
        return claims.get(key, String.class);
    }

    public Integer getInteger(@NonNull String key) {
        return claims.get(key, Integer.class);
    }

    public Long getLong(@NonNull String key) {
        return claims.get(key, Long.class);
    }

    public String subject() {
        return claims.getSubject();
    }

    public Date expiration() {
        return claims.getExpiration();
    }

    public boolean isExpired() {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 只读的 claims 视图。
     */
    public Map<String, Object> claims() {
        return Collections.unmodifiableMap(claims);
    }
}
