package org.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;

import java.util.Date;
import java.util.Map;

/**
 * 快速创建jwt令牌的工具类
 *
 * @author : Tomatos
 * @date : 2025/7/17
 */
public class JwtUtil {

    public static <T> String createJws(
            @NonNull Long expiration,
            @NonNull String secret,
            @NonNull Map<String, Object> payload
    ) {
        long jwsExpiration = System.currentTimeMillis() + expiration * 1000L;
        return Jwts.builder()
                   .expiration(new Date(jwsExpiration))
                   .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                   .claims(payload)
                   .compact();
    }

    /**
     * 解析传入的jwt是否与生成的时候一致
     *
     * @param secret 秘钥字
     * @param jws    加密之后的jwt
     * @return io.jsonwebtoken.Jws<io.jsonwebtoken.Claims>
     * @author : Tomatos
     * @date : 2025/7/22 20:38
     * @since : 1.0
     */
    public static Jws<Claims> parseVerifyJws(@NonNull String secret, @NonNull String jws) throws JwtException {
        return Jwts.parser()
                   .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                   .build() // 构建一个使用secretKey验证JWS的Parser对象
                   .parseSignedClaims(jws); // 解析得到 payload
    }
}
