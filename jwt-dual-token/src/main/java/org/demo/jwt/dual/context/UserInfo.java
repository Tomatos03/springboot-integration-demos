package org.demo.jwt.dual.context;

/**
 * 当前请求的登录用户信息。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public record UserInfo(int userId, String username) {
}
