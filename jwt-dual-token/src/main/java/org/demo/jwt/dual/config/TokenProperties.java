package org.demo.jwt.dual.config;

import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 双 Token 方案的配置载体。
 *
 * <p>密钥以 Base64 编码配置，解码后要求至少 256 bit，避免 HS256 使用弱密钥。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class TokenProperties {

    /**
     * 签发者，写入 iss 声明，解析时强制校验。
     */
    private String issuer = "jwt-dual-token-demo";

    /**
     * Base64 编码的 HMAC-SHA256 密钥，解码后至少 32 字节。
     */
    private String secret;

    /**
     * access token 有效期，单位：秒（默认 15 分钟）。
     */
    private long accessExpirationSeconds = 15L * 60L;

    /**
     * refresh token 有效期，单位：秒（默认 7 天）。
     */
    private long refreshExpirationSeconds = 7L * 24L * 60L * 60L;

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("jwt.issuer must not be blank");
        }
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("jwt.secret must not be blank");
        }
        if (Decoders.BASE64.decode(secret).length < 32) {
            throw new IllegalArgumentException("jwt.secret must be a base64-encoded key of at least 256 bits");
        }
        if (accessExpirationSeconds <= 0) {
            throw new IllegalArgumentException("jwt.access-expiration-seconds must be greater than 0");
        }
        if (refreshExpirationSeconds <= accessExpirationSeconds) {
            throw new IllegalArgumentException(
                    "jwt.refresh-expiration-seconds must be greater than jwt.access-expiration-seconds");
        }
    }
}
