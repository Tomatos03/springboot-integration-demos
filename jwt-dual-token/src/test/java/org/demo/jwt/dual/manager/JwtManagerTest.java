package org.demo.jwt.dual.manager;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import org.demo.jwt.dual.config.TokenProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtManagerTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private JwtManager newJwtManager() {
        TokenProperties properties = new TokenProperties();
        properties.setIssuer("jwt-dual-token-test");
        properties.setSecret(SECRET);
        properties.setAccessExpirationSeconds(60L);
        properties.setRefreshExpirationSeconds(120L);
        return new JwtManager(properties);
    }

    @Test
    void accessTokenShouldCarryIdentityAndMetadata() {
        JwtManager jwtManager = newJwtManager();

        String token = jwtManager.create(TokenType.ACCESS, "42", Map.of("username", "tomato"),
                Duration.ofMinutes(1));
        ParsedToken parsedToken = jwtManager.parse(token, TokenType.ACCESS);

        assertEquals("42", parsedToken.subject());
        assertEquals(42, parsedToken.subjectAsInt());
        assertEquals("tomato", parsedToken.getString("username"));
        assertNotNull(parsedToken.id());
        assertNotNull(parsedToken.issuedAt());
        assertNotNull(parsedToken.expiration());
    }

    @Test
    void refreshTokenShouldBeRejectedWhenAccessTokenExpected() {
        JwtManager jwtManager = newJwtManager();

        String token = jwtManager.create(TokenType.REFRESH, "42", Map.of(), Duration.ofMinutes(2));

        assertThrows(IncorrectClaimException.class, () -> jwtManager.parse(token, TokenType.ACCESS));
    }

    @Test
    void expiredTokenShouldBeRejected() {
        JwtManager jwtManager = newJwtManager();

        String token = jwtManager.create(TokenType.ACCESS, "42", Map.of(), Duration.ofSeconds(-1));

        assertThrows(ExpiredJwtException.class, () -> jwtManager.parse(token, TokenType.ACCESS));
    }

    @Test
    void tamperedTokenShouldBeRejected() {
        JwtManager jwtManager = newJwtManager();

        String token = jwtManager.create(TokenType.ACCESS, "42", Map.of(), Duration.ofMinutes(1));

        assertThrows(JwtException.class, () -> jwtManager.parse(token + "x", TokenType.ACCESS));
    }

    @Test
    void separatelyCreatedTokensShouldHaveDifferentJti() {
        JwtManager jwtManager = newJwtManager();

        String first = jwtManager.create(TokenType.REFRESH, "42", Map.of(), Duration.ofMinutes(2));
        String second = jwtManager.create(TokenType.REFRESH, "42", Map.of(), Duration.ofMinutes(2));

        assertNotEquals(jwtManager.parse(first, TokenType.REFRESH).id(),
                jwtManager.parse(second, TokenType.REFRESH).id());
    }
}
