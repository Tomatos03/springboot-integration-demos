package com.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfigurationSource;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            AccessDeniedHandler accessDeniedHandler,
            AuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        log.info("[OAuth2配置] 1. 配置路径授权规则");
        log.info("[OAuth2配置] 2. 启用OAuth2登录 → 3. 配置登录成功后跳转到 /index.html");
        
        return http.authorizeHttpRequests(auth -> {
                       auth.requestMatchers("/login", "/**.css", "/**.html", "/**.js")
                           .permitAll();
                       auth.anyRequest()
                           .authenticated();
                   })
                   .oauth2Login(oauth2 -> {
                       log.info("[OAuth2配置] OAuth2登录已启用，支持GitHub、Google等第三方登录");
                       oauth2.defaultSuccessUrl("/index.html", true)
                             .permitAll();
                   })
                   .exceptionHandling(exception -> {
                       log.info("[OAuth2配置] 4. 配置异常处理 → 访问拒绝处理 & 认证入口点");
                       exception.accessDeniedHandler(accessDeniedHandler);
                       exception.authenticationEntryPoint(authenticationEntryPoint);
                   })
                   .sessionManagement(session ->
                      session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                   )
                   .cors(cors -> cors.configurationSource(corsConfigurationSource))
                   .csrf(csrf -> csrf.disable())
                   .build();
    }
}