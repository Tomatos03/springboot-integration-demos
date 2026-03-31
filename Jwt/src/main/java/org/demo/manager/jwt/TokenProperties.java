package org.demo.manager.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 *
 *
 * @author : Tomatos
 * @date : 2026/3/31
 */
@Component
@ConfigurationProperties(prefix = "jwt.token")
@Getter
@Setter
public class TokenProperties {
    /**
     * 默认与现有代码保持一致，避免运行行为突变。
     */
    private String secret = "aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=";

    /**
     * token 过期时间，单位：秒（默认 24 小时）。
     */
    private long expirationSeconds = 24L * 60L * 60L;

    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("jwt.token.secret must not be blank");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("jwt.token.expiration-seconds must be greater than 0");
        }
    }
}
