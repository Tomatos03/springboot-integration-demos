package com.demo.controller;

import com.demo.entity.TUser;
import com.demo.service.SmsVerificationService;
import com.demo.utils.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class HelloController {

    private final SmsVerificationService smsVerificationService;

    public HelloController(SmsVerificationService smsVerificationService) {
        this.smsVerificationService = smsVerificationService;
    }

    @PreAuthorize("hasRole('saler')")
    @GetMapping("/hello")
    public String hello() {
        return "Hello";
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/welcome")
    public Principal welcome(Principal principal) {
        return principal;
    }

    @GetMapping("/welcome0")
    public Principal welcome(Authentication authentication) {
        return authentication;
    }

    @GetMapping("/welcome1")
    public TUser welcome() {
        return (TUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestParam String phone) {
        String code = smsVerificationService.generateCode(phone);
        return Result.success("验证码已发送（验证码: " + code + "）", code);
    }
}
