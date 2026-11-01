package org.demo.jwt.dual.dto;

/**
 * 业务失败状态码。
 *
 * <p>HTTP 状态码表达传输/认证层结果，业务状态码表达接口自身的处理结果。
 * 认证类失败使用 401xx 段，便于客户端按号处理。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public enum BizCode {

    ACCESS_TOKEN_MISSING(40101, "缺少 Bearer access token"),
    ACCESS_TOKEN_INVALID(40102, "access token 签名或格式无效"),
    ACCESS_TOKEN_EXPIRED(40103, "access token 已过期"),

    REFRESH_TOKEN_MISSING(40104, "请求中缺少 refresh token"),
    REFRESH_TOKEN_INVALID(40105, "刷新令牌无效或已失效"),
    REFRESH_TOKEN_EXPIRED(40106, "刷新令牌已过期"),
    REFRESH_TOKEN_REUSED(40107, "刷新令牌已被使用，该会话已失效");

    private final int code;
    private final String message;

    BizCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
