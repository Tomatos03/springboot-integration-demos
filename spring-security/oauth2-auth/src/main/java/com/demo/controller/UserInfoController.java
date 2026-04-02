package com.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

/**
 * GitHub API 返回字段: login(用户名), avatar_url(头像), name(显示名称)
 */
@Slf4j
@RestController
public class UserInfoController {
    @GetMapping("/api/userinfo")
    public Map<String, Object> userinfo(@AuthenticationPrincipal Object principal) {
        log.info("[OAuth2流程] 1. 用户登录后访问 /api/userinfo → 2. 从OAuth2Provider获取用户信息 → 3. 返回用户数据");
        Map<String, Object> result = new HashMap<>();
        // 若 OAuth2 登录
        if (principal instanceof OAuth2User oauth2User) {
            log.info("[OAuth2获取用户信息] OAuth2用户认证成功，用户信息: {}", oauth2User.getName());
            Map<String, Object> attrs = oauth2User.getAttributes();
            result.put("username", attrs.getOrDefault("login", ""));
            result.put("avatar_url", attrs.getOrDefault("avatar_url", ""));
            result.put("name", attrs.getOrDefault("name", ""));
            log.info("[OAuth2获取用户信息] 用户信息已提取: username={}", attrs.getOrDefault("login", ""));
        } else {
            log.warn("[OAuth2获取用户信息] 用户未通过OAuth2认证");
            result.put("username", principal != null ? principal.toString() : "anonymous");
        }
        return result;
    }
}
