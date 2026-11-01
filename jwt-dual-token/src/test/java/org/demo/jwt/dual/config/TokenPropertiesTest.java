package org.demo.jwt.dual.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenPropertiesTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    @Test
    void validateShouldRejectBlankSecret() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(" ");

        assertThrows(IllegalArgumentException.class, properties::validate);
    }

    @Test
    void validateShouldRejectWeakSecret() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(Base64.getEncoder().encodeToString(new byte[16]));

        assertThrows(IllegalArgumentException.class, properties::validate);
    }

    @Test
    void validateShouldRejectRefreshNotLongerThanAccess() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(SECRET);
        properties.setAccessExpirationSeconds(600L);
        properties.setRefreshExpirationSeconds(600L);

        assertThrows(IllegalArgumentException.class, properties::validate);
    }

    @Test
    void validateShouldAcceptValidConfiguration() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(SECRET);

        assertDoesNotThrow(properties::validate);
    }
}
