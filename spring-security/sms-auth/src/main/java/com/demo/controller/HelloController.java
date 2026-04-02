package com.demo.controller;

import com.demo.entity.TUser;
import com.demo.service.SmsVerificationService;
import com.demo.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
public class HelloController {

    private final SmsVerificationService smsVerificationService;

    public HelloController(SmsVerificationService smsVerificationService) {
        this.smsVerificationService = smsVerificationService;
    }

    @PreAuthorize("hasRole('saler')")
    @GetMapping("/hello")
    public String hello() {
        log.info("[SmsAuth请求流程] 1. 客户端请求 /hello → 2. SecurityContext中已有认证用户 → 3. 权限检查");
        return "Hello";
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/welcome")
    public Principal welcome(Principal principal) {
        log.info("[SmsAuth流程] 4. 用户权限通过 → 5. 请求被授权执行");
        return principal;
    }

    @GetMapping("/welcome0")
    public Principal welcome(Authentication authentication) {
        log.info("[SmsAuth获取用户信息] 通过方法参数获取认证用户: {}", authentication.getName());
        return authentication;
    }

    @GetMapping("/welcome1")
    public TUser welcome() {
        log.info("[SmsAuth获取用户信息] 通过 SecurityContextHolder 获取认证用户");
        return (TUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestParam String phone) {
        log.info("[SmsAuth流程] 0. 请求发送验证码: phone={}", phone);
        String code = smsVerificationService.generateCode(phone);
        log.info("[SmsAuth流程] 验证码已生成并存储到Redis，客户端可用此code调用 /sms-login");
        return Result.success("验证码已发送（验证码: " + code + "）", code);
    }
}
