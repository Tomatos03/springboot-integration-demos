package org.demo.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserContextTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldStoreAndGetUserInfo() {
        UserInfo userInfo = new UserInfo(1, "demo");

        UserContext.set(userInfo);

        assertSame(userInfo, UserContext.get());
        assertEquals(Integer.valueOf(1), UserContext.getUserId());
    }

    @Test
    void getShouldThrowWhenEmpty() {
        UserContext.clear();

        assertThrows(IllegalStateException.class, UserContext::get);
    }

    @Test
    void clearShouldRemoveUserInfo() {
        UserContext.set(new UserInfo(1, "demo"));

        UserContext.clear();

        assertNull(UserContext.getOrNull());
    }
}
