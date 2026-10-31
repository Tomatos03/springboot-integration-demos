package org.demo.interceptor;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.context.UserContext;
import org.demo.context.UserInfo;
import org.demo.manager.jwt.ParsedToken;
import org.demo.manager.jwt.TokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author : Tomatos
 * @date : 2025/7/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    private static final String TOKEN_PREFIX = "Bearer ";

    private final TokenManager tokenManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isStaticResource(handler)) {
            log.info("请求静态资源，无需 JWT 验证，URI: {}", request.getRequestURI());
            return true;
        }
        UserContext.clear();

        try {
            String token = extractToken(request);
            ParsedToken parsedToken = tokenManager.parse(token);
            UserContext.set(new UserInfo(parsedToken.getInteger("userId"), parsedToken.subject()));
            log.info("请求通过 JWT 验证，URI: {}", request.getRequestURI());
            return true;
        } catch (IllegalArgumentException | JwtException ex) {
            handleAuthFailure(request, response, ex);
            return false;
        }
    }

    private void handleAuthFailure(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        UserContext.clear();

        int status = HttpServletResponse.SC_UNAUTHORIZED;
        String reason = ex.getMessage();
        if (ex instanceof ResponseStatusException responseStatusException) {
            status = responseStatusException.getStatusCode().value();
            reason = responseStatusException.getReason();
        }

        response.setStatus(status);
        log.info("请求未通过 JWT 验证，URI: {}，原因: {}", request.getRequestURI(), reason);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private boolean isStaticResource(Object handler) {
        return !(handler instanceof HandlerMethod);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(TOKEN_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少 Bearer Token");
        }

        String token = authorization.substring(TOKEN_PREFIX.length());
        if (token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer Token 不能为空");
        }
        return token;
    }
}
