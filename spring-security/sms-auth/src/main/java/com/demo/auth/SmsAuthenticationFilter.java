package com.demo.auth;

import cn.hutool.json.JSONUtil;
import com.demo.utils.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 短信认证过滤器
 * 
 * 职责：
 * 1. 拦截 POST /sms-login 请求
 * 2. 从请求参数中提取手机号和验证码
 * 3. 构建未认证的 SmsAuthenticationToken
 * 4. 委托 AuthenticationManager 进行认证
 * 5. 认证成功：设置到 SecurityContext，返回成功响应
 * 6. 认证失败：返回错误响应
 * 
 * 执行时机：
 * - 在 Spring Security 过滤链中执行，在 UsernamePasswordAuthenticationFilter 之前
 * - 对每个请求只执行一次（OncePerRequestFilter）
 * 
 * 比较表单认证：
 * - 表单认证：Spring Security 内置 UsernamePasswordAuthenticationFilter 处理
 * - 短信认证：自定义 SmsAuthenticationFilter 处理
 * 
 * 原因：短信认证需要特定的参数提取逻辑和自定义的 Token 类型
 */
@Slf4j
public class SmsAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    public SmsAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * 核心过滤逻辑
     * 
     * @param request 包含手机号和验证码参数的 HTTP 请求
     * @param response 返回认证结果的 HTTP 响应
     * @param filterChain 过滤链，用于传递给下一个过滤器
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 检查请求是否为 POST /sms-login
        // 只有该请求需要由本 Filter 处理，其他请求直接传递给下一个过滤器
        if (!request.getRequestURI().equals("/sms-login") || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("[SmsAuth流程] 1. 拦截 POST /sms-login 请求");

        // 2. 从请求参数中提取手机号和验证码
        String phone = request.getParameter("phone");
        String code = request.getParameter("code");

        // 3. 参数验证：手机号和验证码都不能为空
        if (phone == null || code == null) {
            log.warn("[SmsAuth流程] 参数验证失败: phone={}, code={}", phone, code);
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("手机号和验证码不能为空")));
            return;
        }

        try {
            log.info("[SmsAuth流程] 2. 提取参数成功，构建未认证Token: phone={}", phone);
            
            // 4. 构建未认证的 SmsAuthenticationToken
            // principal：手机号，credentials：验证码
            SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);
            
            log.info("[SmsAuth流程] 3. 将Token传递给AuthenticationManager进行认证");
            
            // 5. 委托 AuthenticationManager 进行认证
            // AuthenticationManager 会轮询所有 Provider，使用 SmsAuthenticationProvider 处理本 Token
            // - SmsAuthenticationProvider.supports() 返回 true（支持 SmsAuthenticationToken）
            // - SmsAuthenticationProvider.authenticate() 被调用，验证码校验和用户加载
            Authentication authResult = authenticationManager.authenticate(token);
            
            log.info("[SmsAuth流程] 4. SmsAuthenticationProvider验证通过，用户认证成功");
            
            // 6. 认证成功：设置到 SecurityContext
            // 后续请求可通过 SecurityContextHolder.getContext().getAuthentication() 获取当前用户
            SecurityContextHolder.getContext().setAuthentication(authResult);

            // 7. 返回成功响应
            log.info("[SmsAuth流程] 5. 认证信息已设置到SecurityContext，返回成功响应");
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.success("登录成功")));
            
        } catch (AuthenticationException e) {
            // 8. 认证失败：返回错误响应
            // AuthenticationException 包括验证码错误、用户不存在等场景
            log.error("[SmsAuth流程] 认证失败: {}", e.getMessage());
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSONUtil.toJsonStr(Result.error("登录失败: " + e.getMessage())));
        }
    }
}
