package org.demo.jwt.dual.dto;

/**
 * 双 Token 响应体。
 *
 * @param accessToken      访问受保护资源的短效 token
 * @param refreshToken     用于换取新 token 的长效 token
 * @param tokenType        access token 的认证方案，固定为 Bearer
 * @param accessExpiresIn  access token 剩余有效期，单位秒
 * @param refreshExpiresIn refresh token 剩余有效期，单位秒
 * @author : Tomatos
 * @date : 2026/11/1
 */
public record TokenPair(String accessToken, String refreshToken, String tokenType,
                        long accessExpiresIn, long refreshExpiresIn) {
}
