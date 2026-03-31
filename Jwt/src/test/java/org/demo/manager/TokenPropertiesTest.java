package org.demo.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenPropertiesTest {

    @Test
    void validateShouldRejectBlankSecret() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(" ");
        properties.setExpirationSeconds(60L);

        assertThrows(IllegalArgumentException.class, properties::validate);
    }

    @Test
    void validateShouldRejectNonPositiveExpiration() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret("aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=");
        properties.setExpirationSeconds(0L);

        assertThrows(IllegalArgumentException.class, properties::validate);
    }
}
