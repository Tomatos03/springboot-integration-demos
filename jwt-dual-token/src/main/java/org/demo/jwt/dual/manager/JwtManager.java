package org.demo.jwt.dual.manager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.demo.jwt.dual.config.TokenProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 签发与校验的底层实现，只负责签名、有效期和类型声明，不关心业务语义。
 *
 * <p>每枚 token 都会写入 jti、iat、iss 和 tokenType。jti 让 refresh token 可以被
 * 服务端记录、轮换和吊销；tokenType 则保证 refresh token 无法冒充 access token。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@Component
public class JwtManager {

    public static final String TOKEN_TYPE_CLAIM = "tokenType";

    private final TokenProperties properties;
    private final SecretKey signingKey;

    public JwtManager(TokenProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret()));
    }

    /**
     * 签发 token，tokenId 自动生成。
     */
    public String create(TokenType type, String subject, Map<String, Object> claims, Duration ttl) {
        return create(type, subject, UUID.randomUUID().toString(), claims, ttl);
    }

    /**
     * 签发 token，并指定 jti。
     *
     * <p>refresh token 需要把 jti 写入服务端存储，因此由调用方先生成 jti 再签发。</p>
     */
    public String create(TokenType type, String subject, String tokenId,
                         Map<String, Object> claims, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                   .claims(claims == null ? Map.of() : claims)
                   .issuer(properties.getIssuer())
                   .subject(subject)
                   .id(tokenId)
                   .issuedAt(Date.from(now))
                   .expiration(Date.from(now.plus(ttl)))
                   .claim(TOKEN_TYPE_CLAIM, type.value())
                   .signWith(signingKey)
                   .compact();
    }

    /**
     * 解析并校验 token，同时强制校验签发者与 token 类型。
     *
     * @param token        待解析的 JWT
     * @param expectedType 期望的 token 类型
     * @return 已验签的只读 token
     * @throws io.jsonwebtoken.ExpiredJwtException   token 已过期
     * @throws io.jsonwebtoken.IncorrectClaimException 类型、签发者等声明不匹配
     * @throws io.jsonwebtoken.JwtException          签名或格式非法
     */
    public ParsedToken parse(String token, TokenType expectedType) {
        Claims claims = Jwts.parser()
                            .verifyWith(signingKey)
                            .requireIssuer(properties.getIssuer())
                            .require(TOKEN_TYPE_CLAIM, expectedType.value())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return new ParsedToken(claims);
    }
}
