package com.demo.controller;

import com.demo.service.RateLimitService;
import com.demo.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流防刷示例接口
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/ratelimit")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitService rateLimitService;

    /**
     * 接口限流（10 次/分钟）
     * GET /ratelimit/api
     */
    @GetMapping("/api")
    public Map<String, Object> apiLimit() {
        boolean allowed = rateLimitService.tryAccess("demo-api", 10, 60);
        if (allowed) {
            return result("访问成功", null);
        }
        Map<String, Object> map = result("请求过于频繁，请稍后重试", null);
        map.put("code", 429);
        return map;
    }

    /**
     * 短信验证码发送（60 秒间隔）
     * POST /ratelimit/sms?phone=13800138000
     */
    @PostMapping("/sms")
    public Map<String, Object> sendSms(@RequestParam String phone) {
        boolean allowed = rateLimitService.trySendSms(phone);
        if (allowed) {
            return result("验证码已发送", null);
        }
        Long cooldown = rateLimitService.getSmsCooldown(phone);
        Map<String, Object> map = result("请 " + cooldown + " 秒后再试", cooldown);
        map.put("code", 429);
        return map;
    }

    /**
     * IP 限流（100 次/分钟）
     * GET /ratelimit/ip
     */
    @GetMapping("/ip")
    public Map<String, Object> ipLimit(HttpServletRequest request) {
        String ip = IpUtil.getIpAddress(request);
        boolean allowed = rateLimitService.tryIpAccess(ip, 100, 60);
        if (allowed) {
            return result("访问成功，IP: " + ip, null);
        }
        Map<String, Object> map = result("IP " + ip + " 请求过于频繁", null);
        map.put("code", 429);
        return map;
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
