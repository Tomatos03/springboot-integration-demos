package com.demo.config;

import com.demo.handler.MyLoginFailureHandler;
import com.demo.handler.MyLoginSuccessHandler;
import com.demo.handler.MyLogOutSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public MyLoginSuccessHandler myLoginSuccessHandler() {
        return new MyLoginSuccessHandler();
    }

    @Bean
    public MyLoginFailureHandler myLoginFailureHandler() {
        return new MyLoginFailureHandler();
    }

    @Bean
    public MyLogOutSuccessHandler myLogOutSuccessHandler() {
        return new MyLogOutSuccessHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsSource) throws Exception {
        return http
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler(myLoginSuccessHandler())
                        .failureHandler(myLoginFailureHandler()))
                .logout(logout -> logout
                        .logoutSuccessHandler(myLogOutSuccessHandler()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .build();
    }
}
