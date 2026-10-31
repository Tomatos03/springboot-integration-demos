package org.demo.manager;

import org.demo.manager.jwt.JWTManager;
import org.demo.manager.jwt.ParsedToken;
import org.demo.manager.jwt.TokenManager;
import org.demo.manager.jwt.TokenProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtManagerTest {

    private static final String SECRET = "aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=";

    private TokenManager newTokenManager() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(SECRET);
        properties.setExpirationSeconds(60L);
        return new JWTManager(properties);
    }

    @Test
    void createShouldUseConfiguredSecretAndExpirationWithoutDefaultClaims() {
        TokenManager tokenManager = newTokenManager();

        String token = tokenManager.create(Map.of("role", "admin"));

        assertNotNull(token);

        ParsedToken parsedToken = tokenManager.parse(token);
        Object userId = parsedToken.get("userId");
        assertNull(userId);
        assertEquals("admin", parsedToken.getString("role"));
    }

    @Test
    void parseShouldReturnClaimsForValidToken() {
        TokenManager tokenManager = newTokenManager();
        String token = tokenManager.create(Map.of("userId", 99));

        ParsedToken parsedToken = tokenManager.parse(token);

        assertEquals(99, parsedToken.getInteger("userId"));
    }

    @Test
    void validateShouldReturnTrueForValidToken() {
        TokenManager tokenManager = newTokenManager();
        String token = tokenManager.create(Map.of("userId", 1));

        assertTrue(tokenManager.validate(token));
    }

    @Test
    void validateShouldReturnFalseForBlankToken() {
        TokenManager tokenManager = newTokenManager();

        assertFalse(tokenManager.validate(""));
    }

    @Test
    void parseShouldThrowForBlankToken() {
        TokenManager tokenManager = newTokenManager();

        assertThrows(IllegalArgumentException.class, () -> tokenManager.parse(""));
    }
}
