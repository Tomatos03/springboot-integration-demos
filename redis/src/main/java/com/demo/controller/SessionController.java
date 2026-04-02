package com.demo.controller;

import com.demo.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户会话管理接口
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * 登录，获取 Token
     * POST /session/login?userId=1&username=tomatos
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam Long userId, @RequestParam String username) {
        String token = sessionService.login(userId, username);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expireMinutes", 30);
        return result("登录成功", data);
    }

    /**
     * 校验 Token
     * GET /session/verify?token=xxx
     */
    @GetMapping("/verify")
    public Map<String, Object> verify(@RequestParam String token) {
        Object userInfo = sessionService.verifyToken(token);
        if (userInfo != null) {
            return result("Token 有效", userInfo);
        }
        return result("Token 已过期或无效", null);
    }

    /**
     * 登出
     * POST /session/logout?token=xxx
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestParam String token) {
        sessionService.logout(token);
        return result("登出成功", null);
    }

    /**
     * 查询用户是否在线
     * GET /session/online?userId=1
     */
    @GetMapping("/online")
    public Map<String, Object> isOnline(@RequestParam Long userId) {
        boolean online = sessionService.isOnline(userId);
        return result(online ? "用户在线" : "用户不在线", online);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
