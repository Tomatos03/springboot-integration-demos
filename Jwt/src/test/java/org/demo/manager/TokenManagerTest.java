package org.demo.manager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.demo.utils.JwtUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenManagerTest {

    @Test
    void generatorTokenShouldUseConfiguredSecretAndExpirationWithoutDefaultClaims() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret("aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=");
        properties.setExpirationSeconds(60L);

        TokenManager tokenManager = new TokenManager(properties);
        String token = tokenManager.generatorToken(Map.of("role", "admin"));

        assertNotNull(token);

        Jws<Claims> parsed = JwtUtil.parseVerifyJws(properties.getSecret(), token);
        assertNull(parsed.getPayload().get("userId"));
    }

    @Test
    void validateTokenShouldReturnTrueForValidToken() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret("aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=");
        properties.setExpirationSeconds(60L);
        TokenManager tokenManager = new TokenManager(properties);
        String token = tokenManager.generatorToken(Map.of("userId", 1));

        boolean valid = tokenManager.validateToken(token);

        assertTrue(valid);
    }

    @Test
    void validateTokenShouldReturnFalseForBlankToken() {
        TokenProperties properties = new TokenProperties();
        TokenManager tokenManager = new TokenManager(properties);

        boolean valid = tokenManager.validateToken(" ");

        assertFalse(valid);
    }

    @Test
    void parseTokenShouldReturnClaimsForValidToken() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret("aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=");
        properties.setExpirationSeconds(60L);
        TokenManager tokenManager = new TokenManager(properties);
        String token = tokenManager.generatorToken(Map.of("userId", 99));

        Claims claims = tokenManager.parseToken(token);

        assertNotNull(claims);
        assertTrue(claims.containsKey("userId"));
    }

    @Test
    void parseTokenShouldThrowForBlankToken() {
        TokenProperties properties = new TokenProperties();
        TokenManager tokenManager = new TokenManager(properties);

        assertThrows(IllegalArgumentException.class, () -> tokenManager.parseToken(""));
    }
}
