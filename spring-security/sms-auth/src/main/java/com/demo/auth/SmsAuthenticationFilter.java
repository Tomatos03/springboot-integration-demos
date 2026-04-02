package com.demo.auth;

import cn.hutool.json.JSONUtil;
import com.demo.utils.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SmsAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    public SmsAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!request.getRequestURI().equals("/sms-login") || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String phone = request.getParameter("phone");
        String code = request.getParameter("code");

        if (phone == null || code == null) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("手机号和验证码不能为空")));
            return;
        }

        try {
            SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);
            Authentication authResult = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authResult);

            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.success("登录成功")));
        } catch (AuthenticationException e) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("登录失败: " + e.getMessage())));
        }
    }
}
