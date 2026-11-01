package org.demo.jwt.dual.dto;

/**
 * 刷新或登出请求体。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public record RefreshTokenRequest(String refreshToken) {
}
