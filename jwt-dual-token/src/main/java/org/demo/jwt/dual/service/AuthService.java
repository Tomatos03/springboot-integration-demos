package org.demo.jwt.dual.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.jwt.dual.config.TokenProperties;
import org.demo.jwt.dual.dto.BizCode;
import org.demo.jwt.dual.dto.TokenPair;
import org.demo.jwt.dual.exception.AuthException;
import org.demo.jwt.dual.manager.JwtManager;
import org.demo.jwt.dual.manager.ParsedToken;
import org.demo.jwt.dual.manager.TokenType;
import org.demo.jwt.dual.store.RefreshSession;
import org.demo.jwt.dual.store.RefreshTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 双 Token 的签发、刷新与登出。
 *
 * <p>刷新采用「一次性 + 轮换」策略：每次刷新都会签发新的 access token 和 refresh token，
 * 旧 refresh token 立即作废。若旧 token 被再次使用，视为泄露，直接吊销整个令牌族。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtManager jwtManager;
    private final TokenProperties properties;
    private final RefreshTokenStore refreshTokenStore;

    public TokenPair login(int userId, String username) {
        String familyId = UUID.randomUUID()
                              .toString();
        log.info("用户登录，签发双 token，userId={}，familyId={}", userId, familyId);
        return issueTokenPair(userId, username, familyId);
    }

    public TokenPair refresh(String refreshToken) {
        ParsedToken parsedToken = parseRefreshToken(refreshToken);
        String jti = parsedToken.id();
        RefreshSession session = refreshTokenStore.find(jti)
                                                  .orElseThrow(() -> AuthException.unauthorized(
                                                          BizCode.REFRESH_TOKEN_INVALID, "刷新令牌不存在或已失效")
                                                  );

        if (session.expiresAt()
                   .isBefore(Instant.now())) {
            refreshTokenStore.revokeFamily(session.familyId());
            throw AuthException.unauthorized(BizCode.REFRESH_TOKEN_EXPIRED);
        }

        if (!refreshTokenStore.markUsed(jti)) {
            refreshTokenStore.revokeFamily(session.familyId());
            log.warn("检测到 refresh token 重复使用，已吊销令牌族，familyId={}", session.familyId());
            throw AuthException.unauthorized(BizCode.REFRESH_TOKEN_REUSED);
        }

        log.info("刷新 token，userId={}，familyId={}", session.userId(), session.familyId());
        return issueTokenPair(session.userId(), session.username(), session.familyId());
    }

    public void logout(String refreshToken) {
        ParsedToken parsedToken = parseRefreshToken(refreshToken);
        RefreshSession session = refreshTokenStore.find(parsedToken.id())
                                                  .orElseThrow(() -> AuthException.unauthorized(
                                                          BizCode.REFRESH_TOKEN_INVALID, "刷新令牌不存在或已失效"));
        refreshTokenStore.revokeFamily(session.familyId());
        log.info("用户登出，吊销令牌族，userId={}，familyId={}", session.userId(), session.familyId());
    }

    private TokenPair issueTokenPair(int userId, String username, String familyId) {
        String subject = String.valueOf(userId);
        Map<String, Object> claims = Map.of("username", username);

        String accessToken = jwtManager.create(TokenType.ACCESS, subject, claims,
                                               Duration.ofSeconds(properties.getAccessExpirationSeconds()));

        String jti = UUID.randomUUID()
                         .toString();
        String refreshToken = jwtManager.create(TokenType.REFRESH, subject, jti, claims,
                                                Duration.ofSeconds(properties.getRefreshExpirationSeconds()));
        refreshTokenStore.save(new RefreshSession(jti, familyId, userId, username,
                                                  Instant.now()
                                                         .plusSeconds(properties.getRefreshExpirationSeconds()),
                                                  false));

        return new TokenPair(accessToken, refreshToken, "Bearer",
                             properties.getAccessExpirationSeconds(), properties.getRefreshExpirationSeconds());
    }

    private ParsedToken parseRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw AuthException.unauthorized(BizCode.REFRESH_TOKEN_MISSING);
        }
        try {
            return jwtManager.parse(refreshToken, TokenType.REFRESH);
        } catch (ExpiredJwtException ex) {
            throw AuthException.unauthorized(BizCode.REFRESH_TOKEN_EXPIRED);
        } catch (IncorrectClaimException ex) {
            throw AuthException.unauthorized(BizCode.REFRESH_TOKEN_INVALID, "该令牌不是刷新令牌");
        } catch (JwtException | IllegalArgumentException ex) {
            throw AuthException.unauthorized(BizCode.REFRESH_TOKEN_INVALID, "刷新令牌签名或格式无效");
        }
    }
}
