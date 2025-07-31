package com.demo.config;

import com.demo.filter.CaptchaFilter;
import com.demo.filter.JwtFilter;
import com.demo.handler.*;
import com.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * @author : Tomatos
 * @date : 2025/7/28
 */
@Slf4j
@Configuration
@EnableMethodSecurity // 基于注解配置角色访问的资源提供这个注解
public class SecurityConfig {
    @Autowired
    CaptchaFilter captchaFilter;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    MyLoginSuccessHandler loginSuccessHandler;

    @Autowired
    MyLoginFailureHandler loginFailureHandler;

    @Autowired
    MyLogOutSuccessHandler logOutSuccessHandler;

    @Autowired
    MyAccessDeniedHandler accessDeniedHandler;

    @Autowired
    MyAuthenticationEntryPoint myAuthenticationEntryPoint;

    // SpringSecurity 存储或加密密码的时候需要使用到密码加密器, 默认使用BCrypt密码加密器
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(UserService userService) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return urlBasedCorsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {
        return httpSecurity.authorizeHttpRequests((authorize) -> {
                                // 配置需要鉴权的请求, 这里配置了所有请求都需要鉴权之后才能够访问
                               authorize.anyRequest()
                                        .authenticated();

                               // 配置请求需要的角色
//                               authorize.requestMatchers().hasRole()
                               // 配置放行的请求
//                             authorize.requestMatchers().permitAll();
                           })
                           // 开启基于表单的登录
                           .formLogin((formLogin -> {
                               formLogin.loginProcessingUrl("/login")
                                        // 登录表单验证失败时候的处理器
                                        .failureHandler(loginFailureHandler)
                                        // 登录表单验证成功之后的处理器
                                        .successHandler(loginSuccessHandler)
                                        // 表单映射到 用户名 -> username, 密码 -> password
                                        .passwordParameter("password")
                                        .usernameParameter("username");
                           }))
                           .logout(logout -> {
                               // 登出处理地址
                               logout.logoutUrl("/logout");
                               // 用户登出之后的处理器
                               logout.logoutSuccessHandler(logOutSuccessHandler);
                           })
                           .exceptionHandling(exceptionHand -> {
                               // 处理已认证但是权限不足的情况的异常
                               exceptionHand.accessDeniedHandler(accessDeniedHandler);
                               // 处理没有认证的情况的异常
                               exceptionHand.authenticationEntryPoint(myAuthenticationEntryPoint);
                           })
                           // 配置Session策略
                           // 当前Demo配置的是后端分离的项目的权限校验, 不要基于Session, 所以配置Session策略为无
                           // 当关闭了Session机制之后, 不会在自动返回SessionId, Authentication对象不会持久化存储
                           .sessionManagement(session -> {
                               session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                           })
                           // 配置跨域请求
                           .cors(cors -> {
                               cors.configurationSource(corsConfigurationSource);
                           })
                           // 关闭 csrf 跨站请求伪造保护
                           // 如果没有关闭csrf, 前端发送的请求需要拿到spring 生成的csrf_token, 在发送请求时携带上csrf_token
                           .csrf(csrf -> {
                               csrf.disable();
                           })
                           // 添加过滤链, 这里将自定义的Jwt过滤链添加到了LogoutFilter过滤链之前
                           .addFilterBefore(jwtFilter, LogoutFilter.class)
                           // .addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class)
                           .build();
    }
}
