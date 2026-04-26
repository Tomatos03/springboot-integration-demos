package com.example.swagger3.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Minimal security config for demo. Accepts Basic auth (in-memory can be added by users)
 * and a static Bearer token "demo-admin-token" for demo purposes.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(auth -> auth
                           .requestMatchers("/admin/**", "/security/**")
                           .hasRole("ADMIN")
                           .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                           .permitAll()
                           .anyRequest()
                           .permitAll()
                   )
                   .addFilterBefore(demoBearerFilter(), BasicAuthenticationFilter.class)
                   .build();
    }

    @Bean
    public OncePerRequestFilter demoBearerFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null && auth.startsWith("Bearer ")) {
                    String token = auth.substring(7)
                                       .trim();
                    if ("demo-admin-token".equals(token)) {
                        // set authentication with ROLE_ADMIN for demo
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                "demo", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                        SecurityContextHolder.getContext()
                                             .setAuthentication(authentication);
                    }
                }
                filterChain.doFilter(request, response);
            }
        };
    }
}
