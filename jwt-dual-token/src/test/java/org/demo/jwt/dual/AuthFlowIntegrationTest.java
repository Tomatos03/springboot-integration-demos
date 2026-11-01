package org.demo.jwt.dual;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.demo.jwt.dual.dto.Result;
import org.demo.jwt.dual.dto.TokenPair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 覆盖「登录 → 访问 → 刷新 → 重放 → 登出」的完整双 Token 链路。
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    private static final String AUTHORIZATION = "Authorization";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginThenAccessProtectedResource() throws Exception {
        TokenPair tokenPair = login(7, "tomato");

        assertNotNull(tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());

        mockMvc.perform(get("/api/protected").header(AUTHORIZATION, bearer(tokenPair.accessToken())))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.code").value(0))
               .andExpect(jsonPath("$.message").value("success"))
               .andExpect(jsonPath("$.data.userId").value(7))
               .andExpect(jsonPath("$.data.username").value("tomato"))
               .andExpect(jsonPath("$.data.message").value("Hello, tomato!"));
    }

    @Test
    void loginWithoutBodyShouldUseDemoUser() throws Exception {
        String response = mockMvc.perform(post("/api/token/login"))
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$.code").value(0))
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();
        TokenPair tokenPair = readTokenPair(response);

        mockMvc.perform(get("/api/protected").header(AUTHORIZATION, bearer(tokenPair.accessToken())))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.userId").value(1))
               .andExpect(jsonPath("$.data.username").value("demo"));
    }

    @Test
    void protectedResourceShouldRejectRefreshToken() throws Exception {
        TokenPair tokenPair = login(7, "tomato");

        mockMvc.perform(get("/api/protected").header(AUTHORIZATION, bearer(tokenPair.refreshToken())))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40102));
    }

    @Test
    void protectedResourceShouldRejectMissingAndInvalidTokens() throws Exception {
        mockMvc.perform(get("/api/protected"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40101));

        mockMvc.perform(get("/api/protected").header(AUTHORIZATION, "Bearer invalid-token"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40102));
    }

    @Test
    void refreshShouldRotateTokenPair() throws Exception {
        TokenPair first = login(7, "tomato");
        TokenPair second = refresh(first.refreshToken());

        assertNotEquals(first.refreshToken(), second.refreshToken());

        mockMvc.perform(get("/api/protected").header(AUTHORIZATION, bearer(second.accessToken())))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.userId").value(7));
    }

    @Test
    void reusingOldRefreshTokenShouldRevokeWholeFamily() throws Exception {
        TokenPair first = login(7, "tomato");
        TokenPair second = refresh(first.refreshToken());

        mockMvc.perform(post("/api/token/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshBody(first.refreshToken())))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40107));

        mockMvc.perform(post("/api/token/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshBody(second.refreshToken())))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40105));
    }

    @Test
    void logoutShouldRevokeRefreshToken() throws Exception {
        TokenPair tokenPair = login(7, "tomato");

        mockMvc.perform(post("/api/token/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshBody(tokenPair.refreshToken())))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.code").value(0))
               .andExpect(jsonPath("$.message").value("已登出"));

        mockMvc.perform(post("/api/token/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshBody(tokenPair.refreshToken())))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40105));
    }

    @Test
    void refreshEndpointShouldRejectAccessToken() throws Exception {
        TokenPair tokenPair = login(7, "tomato");

        mockMvc.perform(post("/api/token/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshBody(tokenPair.accessToken())))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40105));
    }

    @Test
    void refreshEndpointShouldRejectBlankToken() throws Exception {
        mockMvc.perform(post("/api/token/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshBody(" ")))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.code").value(40104));
    }

    private TokenPair login(int userId, String username) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("userId", userId, "username", username));
        String response = mockMvc.perform(post("/api/token/login")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(body))
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$.code").value(0))
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();
        return readTokenPair(response);
    }

    private TokenPair refresh(String refreshToken) throws Exception {
        String response = mockMvc.perform(post("/api/token/refresh")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(refreshBody(refreshToken)))
                                 .andExpect(status().isOk())
                                 .andExpect(jsonPath("$.code").value(0))
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString();
        return readTokenPair(response);
    }

    private TokenPair readTokenPair(String response) throws Exception {
        Result<TokenPair> result = objectMapper.readValue(response,
                new TypeReference<Result<TokenPair>>() {
                });
        assertEquals(0, result.code());
        assertNotNull(result.data());
        return result.data();
    }

    private String refreshBody(String refreshToken) throws Exception {
        return objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
