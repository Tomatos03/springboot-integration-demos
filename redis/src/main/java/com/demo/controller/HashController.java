package com.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis Hash 类型操作示例
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/hash")
@RequiredArgsConstructor
public class HashController {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 基本存取操作 ====================

    /**
     * 设置单个字段
     * POST /hash/hset?key=user:1&field=name&value=tomatos
     */
    @PostMapping("/hset")
    public Map<String, Object> hSet(@RequestParam String key,
                                    @RequestParam String field,
                                    @RequestParam String value) {
        redisTemplate.opsForHash().put(key, field, value);
        return result("设置字段成功", Map.of(field, value));
    }

    /**
     * 获取单个字段
     * GET /hash/hget?key=user:1&field=name
     */
    @GetMapping("/hget")
    public Map<String, Object> hGet(@RequestParam String key, @RequestParam String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return result("查询字段成功", value);
    }

    /**
     * 批量设置字段
     * POST /hash/hmset?key=user:1
     * Body: {"name":"tomatos","age":"18","city":"beijing"}
     */
    @PostMapping("/hmset")
    public Map<String, Object> hmSet(@RequestParam String key,
                                     @RequestBody Map<String, String> entries) {
        redisTemplate.opsForHash().putAll(key, entries);
        return result("批量设置字段成功", entries);
    }

    // ==================== 获取所有字段 ====================

    /**
     * 获取所有字段和值
     * GET /hash/hgetall?key=user:1
     */
    @GetMapping("/hgetall")
    public Map<String, Object> hGetAll(@RequestParam String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return result("查询所有字段成功", entries);
    }

    /**
     * 获取所有字段名
     * GET /hash/hkeys?key=user:1
     */
    @GetMapping("/hkeys")
    public Map<String, Object> hKeys(@RequestParam String key) {
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        return result("查询所有字段名成功", keys);
    }

    // ==================== 删除字段 ====================

    /**
     * 删除一个或多个字段
     * POST /hash/hdel?key=user:1&fields=name,age
     */
    @PostMapping("/hdel")
    public Map<String, Object> hDel(@RequestParam String key, @RequestParam String[] fields) {
        Long count = redisTemplate.opsForHash().delete(key, (Object[]) fields);
        return result("删除 " + count + " 个字段", count);
    }

    // ==================== 递增字段值 ====================

    /**
     * 递增字段值
     * POST /hash/hincrby?key=user:1&field=age&delta=1
     */
    @PostMapping("/hincrby")
    public Map<String, Object> hIncrBy(@RequestParam String key,
                                       @RequestParam String field,
                                       @RequestParam long delta) {
        Long value = redisTemplate.opsForHash().increment(key, field, delta);
        return result("字段 " + field + " 递增成功", value);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
