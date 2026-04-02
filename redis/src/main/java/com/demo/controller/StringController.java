package com.demo.controller;

import com.demo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis String 类型操作示例
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/string")
@RequiredArgsConstructor
public class StringController {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 基本存取操作 ====================

    /**
     * 设置值
     * POST /string/set?key=name&value=tomatos
     */
    @PostMapping("/set")
    public Map<String, Object> set(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return result("设置成功", value);
    }

    /**
     * 获取值
     * GET /string/get?key=name
     */
    @GetMapping("/get")
    public Map<String, Object> get(@RequestParam String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return result("查询成功", value);
    }

    // ==================== 设置过期时间 ====================

    /**
     * 设置值并指定过期时间（秒）
     * POST /string/setex?key=verifyCode&value=123456&seconds=60
     */
    @PostMapping("/setex")
    public Map<String, Object> setex(@RequestParam String key,
                                     @RequestParam String value,
                                     @RequestParam long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
        return result("设置成功，过期时间 " + seconds + " 秒", value);
    }

    // ==================== 自增/自减 ====================

    /**
     * 自增 1
     * POST /string/incr?key=count
     */
    @PostMapping("/incr")
    public Map<String, Object> incr(@RequestParam String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        return result("自增成功", value);
    }

    /**
     * 自减 1
     * POST /string/decr?key=count
     */
    @PostMapping("/decr")
    public Map<String, Object> decr(@RequestParam String key) {
        Long value = redisTemplate.opsForValue().decrement(key);
        return result("自减成功", value);
    }

    /**
     * 按步长自增
     * POST /string/incrby?key=count&delta=10
     */
    @PostMapping("/incrby")
    public Map<String, Object> incrBy(@RequestParam String key, @RequestParam long delta) {
        Long value = redisTemplate.opsForValue().increment(key, delta);
        return result("按步长 " + delta + " 自增成功", value);
    }

    // ==================== 批量操作 ====================

    /**
     * 批量设置
     * POST /string/mset
     */
    @PostMapping("/mset")
    public Map<String, Object> mSet(@RequestBody Map<String, String> kvMap) {
        redisTemplate.opsForValue().multiSet(kvMap);
        return result("批量设置成功", kvMap);
    }

    /**
     * 批量获取
     * POST /string/mget
     */
    @PostMapping("/mget")
    public Map<String, Object> mGet(@RequestBody List<String> keys) {
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        return result("批量查询成功", values);
    }

    // ==================== 存取对象（序列化） ====================

    /**
     * 存储对象
     * POST /string/set-object
     */
    @PostMapping("/set-object")
    public Map<String, Object> setObject(@RequestBody User user) {
        redisTemplate.opsForValue().set("user:" + user.getId(), user);
        return result("对象存储成功", user);
    }

    /**
     * 获取对象
     * GET /string/get-object?id=1
     */
    @GetMapping("/get-object")
    public Map<String, Object> getObject(@RequestParam Long id) {
        Object user = redisTemplate.opsForValue().get("user:" + id);
        return result("对象查询成功", user);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
