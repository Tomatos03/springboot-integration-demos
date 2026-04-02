package com.demo.controller;

import com.demo.entity.TUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
public class HelloController {
    @PreAuthorize("hasRole('SALER')")
    @GetMapping("/hello")
    public String hello() {
        log.info("[BasicAuth流程] 1. 接收请求 → 2. SecurityFilter验证Authorization头 → 3. 解码Base64用户凭证 → 4. 调用DaoAuthenticationProvider认证");
        return "Hello";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/welcome")
    public Principal welcome(Principal principal) {
        log.info("[BasicAuth流程] 5. 认证成功 → 6. 将Authentication放入SecurityContext → 7. 请求被授权通过");
        return principal;
    }

    @GetMapping("/welcome0")
    public Principal welcome(Authentication authentication) {
        log.info("[BasicAuth获取认证信息] 通过方法参数 Authentication 获取当前认证用户: {}", authentication.getName());
        return authentication;
    }

    @GetMapping("/welcome1")
    public TUser welcome() {
        log.info("[BasicAuth获取认证信息] 通过 SecurityContextHolder 获取当前认证用户");
        return (TUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
