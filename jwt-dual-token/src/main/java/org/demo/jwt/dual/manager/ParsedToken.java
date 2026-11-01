package org.demo.jwt.dual.manager;

import io.jsonwebtoken.Claims;
import lombok.NonNull;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * 已完成签名校验的 JWT 只读对象。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public final class ParsedToken {

    private final Claims claims;

    /**
     * 包级构造器，只允许同包的 {@link JwtManager} 创建，避免包装未经校验的 Claims。
     */
    ParsedToken(@NonNull Claims claims) {
        this.claims = claims;
    }

    public String subject() {
        return claims.getSubject();
    }

    /**
     * 把 sub 声明解析为用户 ID。
     *
     * @return 用户 ID
     * @throws IllegalArgumentException sub 缺失或不是合法整数
     */
    public int subjectAsInt() {
        String subject = claims.getSubject();
        if (subject == null) {
            throw new IllegalArgumentException("token 缺少 sub 声明");
        }
        try {
            return Integer.parseInt(subject);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("token 的 sub 声明不是合法的用户 ID: " + subject, ex);
        }
    }

    public String id() {
        return claims.getId();
    }

    public String getString(@NonNull String key) {
        return claims.get(key, String.class);
    }

    public Date issuedAt() {
        return claims.getIssuedAt();
    }

    public Date expiration() {
        return claims.getExpiration();
    }

    public Map<String, Object> claims() {
        return Collections.unmodifiableMap(claims);
    }
}
