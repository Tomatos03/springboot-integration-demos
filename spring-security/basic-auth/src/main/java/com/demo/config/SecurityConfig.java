package com.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 配置类 - HTTP Basic 认证
 * 
 * 功能：
 * 1. 启用 HTTP Basic 认证（内置支持，无需自定义 Filter 或 Provider）
 * 2. 配置无状态会话管理（STATELESS）
 * 3. 启用 CORS 支持跨域请求
 * 4. 禁用 CSRF 保护
 * 5. 所有资源都需要认证
 * 
 * HTTP Basic 认证原理：
 * 1. 客户端访问受保护资源，未提供凭证
 * 2. 服务端返回 401 Unauthorized + WWW-Authenticate: Basic realm="..."
 * 3. 浏览器/客户端弹出登录对话框，获取用户名和密码
 * 4. 客户端将 username:password 进行 Base64 编码
 * 5. 客户端在 Authorization 请求头中发送 Basic <base64_string>
 * 6. 服务端解码凭证并验证
 * 7. 验证通过返回受保护的资源
 * 
 * 特点：
 * - 无状态：无需 Session，每次请求都需提供凭证
 * - 简单：Spring Security 内置支持
 * - 安全性低：凭证以 Base64 编码发送，应配合 HTTPS 使用
 * - 适用场景：内部服务、API 调试、简单认证
 * 
 * 用户配置参考：
 * 本示例通过实现自定义 UserDetailsService Bean 提供用户信息
 * 可基于内存（InMemoryUserDetailsManager）或数据库实现
 */
@Configuration
public class SecurityConfig {

    /**
     * Spring Security 过滤链配置
     * 
     * 配置说明：
     * - httpBasic()：启用 HTTP Basic 认证
     *   Customizer.withDefaults() 使用默认配置
     *   HTTP Basic 由 Spring Security 内置的 BasicAuthenticationFilter 处理
     * - sessionManagement()：设置 STATELESS，禁用 Session 存储
     *   每次请求都需提供 Authorization 凭证
     * - cors()：启用 CORS，允许跨域请求
     * - csrf()：禁用 CSRF 保护（无状态 API 场景）
     * - authorizeHttpRequests()：所有请求都需要认证
     *   本示例没有公开端点，所有资源都受保护
     * 
     * 用户信息加载：
     * - 需要实现 UserDetailsService Bean（通常在本类或其他 @Configuration 类中）
     * - Spring Security 使用该 Service 根据用户名加载用户信息
     * - UserDetailsService.loadUserByUsername(username) 被调用
     * - 返回的 UserDetails 包含：用户名、密码、权限等信息
     * 
     * 密码验证：
     * - Spring Security 使用 PasswordEncoder 对凭证中的密码进行验证
     * - 比对 Base64 解码后的密码与 UserDetails 中存储的密码哈希
     * - 默认使用 BCryptPasswordEncoder
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        return http
                // 启用 HTTP Basic 认证
                // 由 BasicAuthenticationFilter 处理 Authorization 请求头
                .httpBasic(Customizer.withDefaults())
                // 配置会话管理：无状态，不创建 HTTP Session
                // 每次请求都需在 Authorization 头中提供 Base64 编码的凭证
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 启用 CORS 支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 禁用 CSRF 保护（HTTP Basic 场景通常禁用）
                .csrf(csrf -> csrf.disable())
                // 资源权限配置：所有请求都需要认证
                .authorizeHttpRequests(auth -> auth.anyRequest()
                        .authenticated())
                .build();
    }
}