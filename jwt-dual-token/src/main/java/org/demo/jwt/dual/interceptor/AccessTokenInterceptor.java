package org.demo.jwt.dual.interceptor;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.jwt.dual.context.UserContext;
import org.demo.jwt.dual.context.UserInfo;
import org.demo.jwt.dual.dto.BizCode;
import org.demo.jwt.dual.dto.Result;
import org.demo.jwt.dual.exception.AuthException;
import org.demo.jwt.dual.manager.JwtManager;
import org.demo.jwt.dual.manager.ParsedToken;
import org.demo.jwt.dual.manager.TokenType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 受保护资源的 access token 校验。
 *
 * <p>只接受 tokenType 为 access 的 token；refresh token 不能访问业务接口。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final JSONConfig JSON_CONFIG = JSONConfig.create().setIgnoreNullValue(false);

    private final JwtManager jwtManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        UserContext.clear();

        try {
            String token = extractToken(request);
            ParsedToken parsedToken = jwtManager.parse(token, TokenType.ACCESS);
            String username = parsedToken.getString("username");
            UserContext.set(new UserInfo(parsedToken.subjectAsInt(), username == null ? "unknown" : username));
            log.info("请求通过 access token 校验，URI: {}", request.getRequestURI());
            return true;
        } catch (ExpiredJwtException ex) {
            writeUnauthorized(response, BizCode.ACCESS_TOKEN_EXPIRED);
        } catch (IncorrectClaimException ex) {
            writeUnauthorized(response, BizCode.ACCESS_TOKEN_INVALID, "令牌类型或签发者不匹配");
        } catch (AuthException ex) {
            writeUnauthorized(response, ex.getBizCode(), ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            writeUnauthorized(response, BizCode.ACCESS_TOKEN_INVALID);
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 从 Authorization 请求头中提取 Bearer token。
     *
     * @throws AuthException 请求头缺失、格式不是 Bearer 或 token 为空
     */
    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw AuthException.unauthorized(BizCode.ACCESS_TOKEN_MISSING);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw AuthException.unauthorized(BizCode.ACCESS_TOKEN_MISSING);
        }
        return token;
    }

    private void writeUnauthorized(HttpServletResponse response, BizCode bizCode) throws IOException {
        writeUnauthorized(response, bizCode, bizCode.message());
    }

    private void writeUnauthorized(HttpServletResponse response, BizCode bizCode, String message)
            throws IOException {
        UserContext.clear();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JSONUtil.toJsonStr(
                Result.<Void>failure(bizCode, message), JSON_CONFIG));
        log.info("请求未通过 access token 校验，code={}，message={}", bizCode.code(), message);
    }
}
