package org.demo;

import org.demo.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证拦截器解析、UserContext 写入和请求结束清理的完整链路。
 */
@SpringBootTest
@AutoConfigureMockMvc
class JwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void clearUserContext() {
        UserContext.clear();
    }

    @Test
    void accessResourceShouldAcceptValidToken() throws Exception {
        String token = mockMvc.perform(get("/getToken"))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse()
                              .getContentAsString();

        mockMvc.perform(get("/accessResource")
                                .header("Authorization", "Bearer " + token))
               .andExpect(status().isOk())
               .andExpect(content().string("Hello World!"));

        assertNull(UserContext.getOrNull());
    }

    @Test
    void accessResourceShouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/accessResource"))
               .andExpect(status().isUnauthorized());

        assertNull(UserContext.getOrNull());
    }

    @Test
    void accessResourceShouldReturnUnauthorizedWhenTokenInvalid() throws Exception {
        mockMvc.perform(get("/accessResource")
                                .header("Authorization", "Bearer invalid-token"))
               .andExpect(status().isUnauthorized());

        assertNull(UserContext.getOrNull());
    }
}
