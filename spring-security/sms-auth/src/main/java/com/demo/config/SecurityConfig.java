package com.demo.config;

import com.demo.auth.SmsAuthenticationFilter;
import com.demo.auth.SmsAuthenticationProvider;
import com.demo.service.SmsVerificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SmsAuthenticationProvider smsAuthenticationProvider(UserDetailsService userDetailsService,
                                                               SmsVerificationService smsVerificationService) {
        return new SmsAuthenticationProvider(userDetailsService, smsVerificationService);
    }

    @Bean
    public AuthenticationManager authenticationManager(SmsAuthenticationProvider smsProvider) {
        return new ProviderManager(smsProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsSource,
                                                   AuthenticationManager authenticationManager) throws Exception {
        SmsAuthenticationFilter smsFilter = new SmsAuthenticationFilter(authenticationManager);

        return http
                .addFilterBefore(smsFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/sms-login", "/send-code").permitAll();
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsSource))
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
