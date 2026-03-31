package org.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.manager.jwt.TokenManager;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author : Tomatos
 * @date : 2025/7/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    private final TokenManager tokenManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isStaticResource(handler)) {
            log.info("请求静态资源，无需 JWT 验证，URI: {}", request.getRequestURI());
            return true;
        }

        String token;
        if (
                ((token = extractToken(request)) != null)
                && tokenManager.validateToken(token)
        ) {
            log.info("请求通过 JWT 验证，URI: {}", request.getRequestURI());
            return true;
        }
        log.info("请求未通过 JWT 验证，URI: {}", request.getRequestURI());
        return false;
    }

    private boolean isStaticResource(Object handler) {
        return !(handler instanceof HandlerMethod);
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            // 提取 token 部分
            // "Bearer " 长度为7
            return authorization.substring(7);
        } else {
            // 没有携带token或格式不正确，可以返回null或抛异常
            return null;
        }
    }
}
