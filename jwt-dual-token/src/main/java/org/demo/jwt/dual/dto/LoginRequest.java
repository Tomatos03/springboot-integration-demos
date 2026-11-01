package org.demo.jwt.dual.dto;

/**
 * 登录请求。字段全部可选，缺省时使用演示用户。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public record LoginRequest(Integer userId, String username) {

    public LoginRequest {
        if (userId == null || userId <= 0) {
            userId = 1;
        }
        if (username == null || username.isBlank()) {
            username = "demo";
        }
    }
}
