package org.demo.manager.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * {@link TokenManager} 的默认实现，基于 {@link TokenProperties} 完成令牌的创建和解析。
 *
 * @author : Tomatos
 * @date : 2026/3/31
 */
@Component
@RequiredArgsConstructor
public class JWTManager implements TokenManager {
    private final TokenProperties tokenProperties;

    @Override
    public String create(@NonNull Map<String, Object> payload) {
        long expiration = System.currentTimeMillis() + tokenProperties.getExpirationSeconds() * 1000L;
        return Jwts.builder()
                   .expiration(new Date(expiration))
                   .signWith(Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes()))
                   .claims(payload)
                   .compact();
    }

    @Override
    public ParsedToken parse(@NonNull String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes()))
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return new ParsedToken(claims);
    }
}
