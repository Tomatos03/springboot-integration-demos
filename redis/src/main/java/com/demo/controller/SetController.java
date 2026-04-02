package com.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Redis Set 类型操作示例
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/set")
@RequiredArgsConstructor
public class SetController {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 添加/删除元素 ====================

    /**
     * 添加元素
     * POST /set/sadd?key=fruits&members=apple,banana,orange
     */
    @PostMapping("/sadd")
    public Map<String, Object> sAdd(@RequestParam String key, @RequestParam String[] members) {
        Long count = redisTemplate.opsForSet().add(key, (Object[]) members);
        return result("添加 " + count + " 个元素", count);
    }

    /**
     * 删除元素
     * POST /set/srem?key=fruits&members=apple
     */
    @PostMapping("/srem")
    public Map<String, Object> sRem(@RequestParam String key, @RequestParam String[] members) {
        Long count = redisTemplate.opsForSet().remove(key, (Object[]) members);
        return result("删除 " + count + " 个元素", count);
    }

    // ==================== 判断元素是否存在 ====================

    /**
     * 判断元素是否存在
     * GET /set/sismember?key=fruits&member=apple
     */
    @GetMapping("/sismember")
    public Map<String, Object> sIsMember(@RequestParam String key, @RequestParam String member) {
        Boolean exists = redisTemplate.opsForSet().isMember(key, member);
        return result("元素 " + member + (exists ? " 存在" : " 不存在"), exists);
    }

    // ==================== 集合运算 ====================

    /**
     * 交集
     * POST /set/sinter?key1=fruits&key2=redFruits
     */
    @PostMapping("/sinter")
    public Map<String, Object> sIntersect(@RequestParam String key1, @RequestParam String key2) {
        Set<Object> result = redisTemplate.opsForSet().intersect(key1, key2);
        return result("交集运算成功", result);
    }

    /**
     * 并集
     * POST /set/sunion?key1=fruits&key2=vegetables
     */
    @PostMapping("/sunion")
    public Map<String, Object> sUnion(@RequestParam String key1, @RequestParam String key2) {
        Set<Object> result = redisTemplate.opsForSet().union(key1, key2);
        return result("并集运算成功", result);
    }

    /**
     * 差集
     * POST /set/sdiff?key1=fruits&key2=redFruits
     */
    @PostMapping("/sdiff")
    public Map<String, Object> sDiff(@RequestParam String key1, @RequestParam String key2) {
        Set<Object> result = redisTemplate.opsForSet().difference(key1, key2);
        return result("差集运算成功", result);
    }

    // ==================== 获取集合大小 ====================

    /**
     * 获取集合大小
     * GET /set/scard?key=fruits
     */
    @GetMapping("/scard")
    public Map<String, Object> sCard(@RequestParam String key) {
        Long size = redisTemplate.opsForSet().size(key);
        return result("集合大小", size);
    }

    /**
     * 获取集合所有元素
     * GET /set/smembers?key=fruits
     */
    @GetMapping("/smembers")
    public Map<String, Object> sMembers(@RequestParam String key) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        return result("查询所有元素成功", members);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
