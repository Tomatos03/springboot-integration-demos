package com.demo.controller;

import com.demo.entity.User;
import com.demo.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存相关示例接口
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    /**
     * 查询用户（先查缓存再查库）
     * GET /cache/user?id=1
     */
    @GetMapping("/user")
    public Map<String, Object> getUser(@RequestParam Long id) {
        Object user = cacheService.getUser(id);
        return result("查询成功", user);
    }

    /**
     * 查询用户（带缓存穿透防护）
     * GET /cache/user-safe?id=999
     */
    @GetMapping("/user-safe")
    public Map<String, Object> getUserSafe(@RequestParam Long id) {
        Object user = cacheService.getUserSafe(id);
        return result("查询成功（穿透防护）", user);
    }

    /**
     * 更新用户并删除缓存
     * POST /cache/user/update
     */
    @PostMapping("/user/update")
    public Map<String, Object> updateUser(@RequestBody User user) {
        cacheService.updateUser(user);
        return result("更新成功，缓存已删除", user);
    }

    /**
     * 查看缓存剩余过期时间
     * GET /cache/user/expire?id=1
     */
    @GetMapping("/user/expire")
    public Map<String, Object> getExpire(@RequestParam Long id) {
        Long seconds = cacheService.getExpire(id);
        return result("缓存剩余过期时间（秒）", seconds);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
