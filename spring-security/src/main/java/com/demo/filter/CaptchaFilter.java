package com.demo.filter;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author : Tomatos
 * @date : 2025/7/29
 */
@Component
public class CaptchaFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (!requestURI.equals("/doLogin")) {
            filterChain.doFilter(request, response);
            return;
        }
        String code = (String) request.getSession().getAttribute("captcha");
        if (StrUtil.isEmpty(code)) {
            response.sendRedirect("/myLogin");
            return;
        }
        String userCode = request.getParameter("captcha");
        if (!userCode.equalsIgnoreCase(code)) {
            response.sendRedirect("/myLogin");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
