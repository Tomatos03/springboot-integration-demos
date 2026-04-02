package com.demo.config;

import com.demo.auth.SmsAuthenticationFilter;
import com.demo.auth.SmsAuthenticationProvider;
import com.demo.service.SmsVerificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 配置类 - 短信认证
 *
 * 功能：
 * 1. 注册自定义的短信认证 Provider（验证码校验 + 用户加载）
 * 2. 配置自定义 AuthenticationManager 使用 SmsAuthenticationProvider
 * 3. 注册自定义短信认证 Filter（拦截 POST /sms-login 请求）
 * 4. 配置 /sms-login 和 /send-code 为公开端点
 * 5. 其他所有资源需要认证
 *
 * 认证流程：
 * 用户提交手机号 + 验证码 → SmsAuthenticationFilter 拦截 /sms-login
 * → 构建 SmsAuthenticationToken（未认证状态）
 * → 委托 AuthenticationManager 处理
 * → SmsAuthenticationProvider 验证码校验（调用 SmsVerificationService）
 * → 校验通过后加载用户信息（调用 UserDetailsService）
 * → 返回已认证的 SmsAuthenticationToken
 * → 设置到 SecurityContext 中
 */
@Configuration
public class SecurityConfig {

    /**
     * 短信认证 Provider Bean
     *
     * 职责：
     * 1. 实现 AuthenticationProvider 接口
     * 2. 从未认证的 SmsAuthenticationToken 中提取手机号和验证码
     * 3. 调用 SmsVerificationService 验证验证码有效性
     * 4. 若验证码有效，调用 UserDetailsService 加载用户信息
     * 5. 返回已认证的 Token（包含权限信息）
     *
     * 参数：
     * - userDetailsService：用于根据手机号加载用户信息
     * - smsVerificationService：用于验证码校验
     */
    @Bean
    public SmsAuthenticationProvider smsAuthenticationProvider(UserDetailsService userDetailsService,
                                                               SmsVerificationService smsVerificationService) {
        return new SmsAuthenticationProvider(userDetailsService, smsVerificationService);
    }

    /**
     * 认证管理器 Bean
     *
     * 职责：
     * 1. 管理多个 AuthenticationProvider（本示例只有 SmsAuthenticationProvider）
     * 2. 当 authenticate() 被调用时，轮询所有 Provider，找到支持该 Token 类型的 Provider 进行处理
     * 3. SmsAuthenticationProvider 通过 supports() 方法声明支持 SmsAuthenticationToken
     *
     * 使用 ProviderManager：支持多个 Provider，按顺序尝试认证
     */
    @Bean
    public AuthenticationManager authenticationManager(SmsAuthenticationProvider smsProvider) {
        return new ProviderManager(smsProvider);
    }

    /**
     * Spring Security 过滤链配置
     *
     * 配置说明：
     * - addFilterBefore()：在 UsernamePasswordAuthenticationFilter 之前添加 SmsAuthenticationFilter
     *   确保短信认证 Filter 优先执行，拦截 /sms-login 请求
     * - authorizeHttpRequests()：配置资源权限
     *   - /sms-login, /send-code：公开，任何人可访问
     *   - 其他请求：需要认证
     * - sessionManagement()：设置 STATELESS，禁用 Session 存储
     * - cors()：启用 CORS，允许跨域请求
     * - csrf()：禁用 CSRF 保护（短信认证场景）
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   AuthenticationManager authenticationManager) throws Exception {
        // 创建自定义短信认证 Filter
        // 该 Filter 拦截 POST /sms-login 请求，提取手机号和验证码
        SmsAuthenticationFilter smsFilter = new SmsAuthenticationFilter(authenticationManager);

        return http
                // 在 UsernamePasswordAuthenticationFilter 之前添加 SmsAuthenticationFilter
                // 确保短信认证优先处理 /sms-login 请求
                .addFilterBefore(smsFilter, UsernamePasswordAuthenticationFilter.class)
                // 配置请求权限
                .authorizeHttpRequests(auth -> {
                    // /sms-login 和 /send-code 为公开端点
                    auth.requestMatchers("/sms-login", "/send-code").permitAll();
                    // 其他所有请求需要认证
                    auth.anyRequest().authenticated();
                })
                // 配置会话管理：无状态，不创建 HTTP Session
                // 每次请求都通过 Token 进行认证
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 启用 CORS 支持
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // 禁用 CSRF 保护（短信认证无需 CSRF token）
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
