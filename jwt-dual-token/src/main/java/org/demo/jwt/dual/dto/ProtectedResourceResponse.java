package org.demo.jwt.dual.dto;

/**
 * 受保护资源的响应数据。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public record ProtectedResourceResponse(String message, int userId, String username) {
}
