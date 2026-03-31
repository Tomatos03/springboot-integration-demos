package org.demo.manager.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.demo.utils.JwtUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/3/31
 */
@Component
@RequiredArgsConstructor
public class TokenManager {
    private final TokenProperties tokenProperties;

    public String generatorToken(@NonNull Map<String, Object> payload) {
        return JwtUtil.createJws(
                tokenProperties.getExpirationSeconds(),
                tokenProperties.getSecret(),
                payload
        );
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (IllegalArgumentException | JwtException ex) {
            return false;
        }
    }

    public Claims parseToken(@NonNull String token) {
        return JwtUtil.parseVerifyJws(tokenProperties.getSecret(), token).getPayload();
    }
}
