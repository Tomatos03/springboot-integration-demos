package org.demo.manager.jwt;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;

import java.util.Map;

/**
 * JWT 令牌管理接口，统一令牌的创建与解析入口。
 *
 * @author : Tomatos
 * @date : 2026/3/31
 */
public interface TokenManager {

    /**
     * 创建 JWT 令牌。
     *
     * @param payload 需要写入令牌的自定义声明
     * @return 签名后的 JWT 字符串
     */
    String create(Map<String, Object> payload);

    /**
     * 解析并校验 JWT 令牌。
     *
     * @param token 待解析的 JWT 字符串
     * @return 已验签的令牌对象
     * @throws IllegalArgumentException 令牌为空或格式非法
     */
    ParsedToken parse(String token) throws IllegalArgumentException;

    /**
     * 校验 JWT 令牌是否有效。
     *
     * @param token 待校验的 JWT 字符串
     * @return 令牌有效返回 true，否则返回 false
     */
    default boolean validate(String token) {
        if (token == null) {
            return false;
        }
        try {
            parse(token);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
