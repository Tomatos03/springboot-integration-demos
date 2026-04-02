package com.demo.filter;

import cn.hutool.jwt.JWTUtil;
import com.demo.entity.TUser;
import com.demo.service.TokenRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JWT Token 验证过滤器
 * 
 * 职责：
 * 1. 从请求头中提取 JWT Token
 * 2. 验证 Token 的有效性（签名和过期时间）
 * 3. 检查 Token 是否存在于 Redis（防止已登出的 Token 使用）
 * 4. 若验证通过，解析 Token 中的用户信息
 * 5. 构建 Authentication 对象并设置到 SecurityContext 中
 * 6. 允许后续业务逻辑访问当前登录用户信息
 * 
 * 执行时机：
 * - 在 Spring Security 过滤链之前执行
 * - 对每个请求只执行一次（OncePerRequestFilter）
 * - 登录、登出请求会跳过验证
 * 
 * Token 格式：
 * 请求头中包含 token: <jwt-token>
 * 本过滤器从 token 请求头中提取 Token 字符串
 * 
 * 验证流程：
 * 1. 提取 Token 字符串
 * 2. 验证 Token 签名有效性（使用配置的 secret）
 * 3. 检查 Token 是否已过期（JWT 内置过期时间）
 * 4. 检查 Token 是否存在于 Redis（防止已删除的 Token）
 * 5. 若全部通过，解析用户信息并设置认证
 * 
 * 验证失败处理：
 * - Token 不存在：返回 403 Forbidden
 * - 签名验证失败：返回 403 Forbidden
 * - Token 已过期：返回 403 Forbidden
 * - Token 不在 Redis 中（已登出）：返回 403 Forbidden
 * 客户端需要重新登录获取新 Token
 */
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:defaultSecretKey123456}")
    private String secret;

    private final TokenRedisService tokenRedisService;

    public JwtFilter(TokenRedisService tokenRedisService) {
        this.tokenRedisService = tokenRedisService;
    }

    /**
     * 核心过滤逻辑
     * 
     * @param request  HTTP 请求对象
     * @param response HTTP 响应对象
     * @param filterChain 过滤链，用于传递给下一个过滤器
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        
        // 1. 跳过登录和登出请求
        // 原因：这两个请求不需要 Token 验证
        if (requestURI.contains("/login") || requestURI.contains("/logout")) {
            log.info("[FormAuth+JWT流程] 1. 跳过登录/登出请求: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 从请求头中获取 Token
        // 格式：token: <jwt-token>
        String token = request.getHeader("token");
        
        // 3. 验证 Token 有效性
        // Token 不存在或签名验证失败：返回 403 Forbidden
        if (token == null || !JWTUtil.verify(token, secret.getBytes())) {
            log.warn("[FormAuth+JWT流程] Token验证失败，拒绝访问: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 4. 检查 Token 是否存在于 Redis
        // 防止使用已登出的 Token
        // Redis 中没有该 Token 表示已被删除（登出）或已过期
        if (!tokenRedisService.isTokenExists(token)) {
            log.warn("[FormAuth+JWT流程] Token不存在于Redis（可能已登出）: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 5. Token 有效，解析用户信息
        // 从 Token 中提取 payload 中的 user 对象
        Map userObj = (Map) JWTUtil.parseToken(token).getPayload("user");
        
        // 6. 构建用户对象
        TUser user = new TUser();
        user.setLoginAct((String) userObj.get("loginAct"));
        user.setName((String) userObj.get("name"));

        // 7. 构建权限列表
        // 说明：本示例使用 ROLE_USER，实际应用中可从 Token 或数据库读取真实权限
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        // 8. 创建 Authentication 对象
        // 参数：principal（用户），credentials（密码，此处为 null），authorities（权限）
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
        
        // 9. 设置到 SecurityContext 中
        // 这样后续业务逻辑可通过 SecurityContextHolder.getContext().getAuthentication() 访问
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        log.info("[FormAuth+JWT流程] 2. Token验证成功，用户信息已恢复: {}", user.getLoginAct());
        
        // 10. 继续传递给下一个过滤器
        filterChain.doFilter(request, response);
    }
}
