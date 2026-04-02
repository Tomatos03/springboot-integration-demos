package com.demo.config;

import com.demo.handler.CustomLoginFailureHandler;
import com.demo.handler.CustomLoginSuccessHandler;
import com.demo.handler.CustomLogoutSuccessHandler;
import com.demo.service.TokenRedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 配置类 - 表单认证
 * 
 * 功能：
 * 1. 启用基于用户名密码的表单登录
 * 2. 注册自定义的成功/失败处理器，签发 JWT Token
 * 3. 配置无状态会话管理（STATELESS）
 * 4. 启用 CORS 支持跨域请求
 * 5. 禁用 CSRF 保护（JWT 场景下通常禁用）
 * 
 * 认证流程：
 * 用户提交表单 → Spring Security 拦截 /login → AuthenticationManager 验证凭证
 * → 成功：CustomLoginSuccessHandler 签发 JWT Token
 * → 失败：CustomLoginFailureHandler 返回错误响应
 * 
 * 后续请求：JwtFilter 从请求头提取并验证 Token
 */
@Configuration
public class SecurityConfig {

    /**
     * 登录成功处理器
     * 职责：认证成功后，签发 JWT Token 并存储到 Redis
     * 返回给客户端：{ code: 200, msg: "登录成功", data: { token: "..." } }
     */
    @Bean
    public CustomLoginSuccessHandler customLoginSuccessHandler(TokenRedisService tokenRedisService) {
        return new CustomLoginSuccessHandler(tokenRedisService);
    }

    /**
     * 登录失败处理器
     * 职责：认证失败时返回错误响应
     * 返回给客户端：{ code: 500, msg: "登录失败" }
     */
    @Bean
    public CustomLoginFailureHandler customLoginFailureHandler() {
        return new CustomLoginFailureHandler();
    }

    /**
     * 登出成功处理器
     * 职责：登出时从 Redis 中删除 Token 并返回成功响应
     * 返回给客户端：{ code: 200, msg: "登出成功" }
     */
    @Bean
    public CustomLogoutSuccessHandler customLogoutSuccessHandler(TokenRedisService tokenRedisService) {
        return new CustomLogoutSuccessHandler(tokenRedisService);
    }

    /**
     * Spring Security 过滤链配置
     * 
     * 配置说明：
     * - formLogin()：启用表单登录，设置登录处理地址和成功/失败处理器
     * - logout()：配置登出处理
     * - sessionManagement()：设置 STATELESS，禁用 Session 存储
     * - cors()：启用 CORS，允许跨域请求
     * - csrf()：禁用 CSRF 保护（JWT 场景）
     * - authorizeHttpRequests()：所有请求需要认证
     * 
     * 注意：JwtFilter 在此链之前执行，验证后续请求中的 Token
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsSource,
                                                   CustomLoginSuccessHandler loginSuccessHandler,
                                                   CustomLoginFailureHandler loginFailureHandler,
                                                   CustomLogoutSuccessHandler logoutSuccessHandler) throws Exception {
        return http
                // 启用表单登录
                .formLogin(form -> form
                        // 登录表单提交地址
                        .loginProcessingUrl("/login")
                        // 认证成功时调用 CustomLoginSuccessHandler 签发 JWT Token
                        .successHandler(loginSuccessHandler)
                        // 认证失败时调用 CustomLoginFailureHandler 返回错误
                        .failureHandler(loginFailureHandler))
                // 配置登出处理
                .logout(logout -> logout
                        // 登出成功时调用处理器返回成功响应
                        .logoutSuccessHandler(logoutSuccessHandler))
                // 配置会话管理策略
                .sessionManagement(session ->
                        // STATELESS：无状态，不创建或使用 HTTP Session
                        // 每次请求都通过 Token 进行认证
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 启用 CORS 支持
                .cors(cors -> cors.configurationSource(corsSource))
                // 禁用 CSRF 保护（使用 JWT 时无需 CSRF token）
                .csrf(csrf -> csrf.disable())
                // 资源权限配置：所有请求都需要认证
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .build();
    }
}
