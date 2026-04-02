package com.demo.filter;

import cn.hutool.jwt.JWTUtil;
import com.demo.entity.TUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:defaultSecretKey123456}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/login") || requestURI.contains("/logout")
                || requestURI.contains("/sms-login") || requestURI.contains("/send-code")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("token");
        if (token == null || !JWTUtil.verify(token, secret.getBytes())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Map userObj = (Map) JWTUtil.parseToken(token).getPayload("user");
        TUser user = new TUser();
        user.setLoginAct((String) userObj.get("loginAct"));
        user.setName((String) userObj.get("name"));

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}