package com.demo.controller;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */

import com.demo.entity.TUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author : Tomatos
 * @date : 2025/7/13
 */
@Slf4j
@RestController
public class HelloController {
    @PreAuthorize("hasRole('saler')")
    @GetMapping("/hello")
    public String hello() {
        log.info("[FormAuth+JWT请求流程] 1. 客户端请求 /hello → 2. JwtFilter验证Token → 3. 恢复用户认证状态 → 4. 权限检查");
        return "Hello";
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/welcome")
    public Principal welcome(Principal principal) {
        log.info("[FormAuth+JWT流程] 5. 用户权限通过 → 6. 请求被授权执行");
        return principal;
    }

    @GetMapping("/welcome0")
    public Principal welcome(Authentication authentication) {
        log.info("[FormAuth+JWT获取用户信息] 通过方法参数获取认证用户: {}", authentication.getName());
        return authentication;
    }

    @GetMapping("/welcome1")
    public TUser welcome() {
        log.info("[FormAuth+JWT获取用户信息] 通过 SecurityContextHolder 获取认证用户");
        return (TUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
