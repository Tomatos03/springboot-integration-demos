package org.demo.context;

/**
 * 当前登录用户信息。
 *
 * @param userId  用户 ID
 * @param subject JWT 主题
 * @author : Tomatos
 * @date : 2026/3/31
 */
public record UserInfo(Integer userId, String subject) {
}
