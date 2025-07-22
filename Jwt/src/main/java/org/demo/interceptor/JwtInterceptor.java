package org.demo.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.demo.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author : Tomatos
 * @date : 2025/7/22
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Interceptor默认不拦截静态资源, 为了代码健壮性添加本行代码
        if (!(handler instanceof HandlerMethod)) {
            // 当前请求不是 Controller 方法（比如静态资源），直接放行
            return true;
        }

        log.info("尝试解析Token...");
        // 这里简化了token从请求中的获取
        String token = request.getCookies()[0].getValue();

        if (token == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        Jws<Claims> claimsJws;
        try {
            claimsJws = JwtUtil.parseVerifyJws("aYyXSHX2tcBhZFNH6H9XAAXpk7tZAQmLtwserrmRk98=",
                                               token);
        } catch (JwtException ex) {
            // jwt过期或不匹配时抛出异常
            log.info("解析Token失败");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        log.info("解析Token成功");
        Claims payload = claimsJws.getPayload();
        // 获取相应信息
        return true;
    }
}
