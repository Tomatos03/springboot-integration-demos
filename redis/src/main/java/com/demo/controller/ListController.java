package com.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis List 类型操作示例
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/list")
@RequiredArgsConstructor
public class ListController {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 左右推入/弹出 ====================

    /**
     * 左侧推入（头部）
     * POST /list/lpush?key=myList&values=a,b,c
     */
    @PostMapping("/lpush")
    public Map<String, Object> lPush(@RequestParam String key, @RequestParam String[] values) {
        Long size = redisTemplate.opsForList().leftPushAll(key, (Object[]) values);
        return result("左侧推入成功", size);
    }

    /**
     * 右侧推入（尾部）
     * POST /list/rpush?key=myList&values=x,y,z
     */
    @PostMapping("/rpush")
    public Map<String, Object> rPush(@RequestParam String key, @RequestParam String[] values) {
        Long size = redisTemplate.opsForList().rightPushAll(key, (Object[]) values);
        return result("右侧推入成功", size);
    }

    /**
     * 左侧弹出
     * POST /list/lpop?key=myList
     */
    @PostMapping("/lpop")
    public Map<String, Object> lPop(@RequestParam String key) {
        Object value = redisTemplate.opsForList().leftPop(key);
        return result("左侧弹出成功", value);
    }

    /**
     * 右侧弹出
     * POST /list/rpop?key=myList
     */
    @PostMapping("/rpop")
    public Map<String, Object> rPop(@RequestParam String key) {
        Object value = redisTemplate.opsForList().rightPop(key);
        return result("右侧弹出成功", value);
    }

    // ==================== 获取范围元素 ====================

    /**
     * 获取范围元素
     * GET /list/lrange?key=myList&start=0&end=-1
     */
    @GetMapping("/lrange")
    public Map<String, Object> lRange(@RequestParam String key,
                                      @RequestParam long start,
                                      @RequestParam long end) {
        List<Object> list = redisTemplate.opsForList().range(key, start, end);
        return result("查询范围成功", list);
    }

    // ==================== 获取列表长度 ====================

    /**
     * 获取列表长度
     * GET /list/llen?key=myList
     */
    @GetMapping("/llen")
    public Map<String, Object> lLen(@RequestParam String key) {
        Long size = redisTemplate.opsForList().size(key);
        return result("查询长度成功", size);
    }

    // ==================== 阻塞弹出 ====================

    /**
     * 阻塞左侧弹出（等待指定秒数）
     * POST /list/blpop?key=myList&timeout=10
     */
    @PostMapping("/blpop")
    public Map<String, Object> blPop(@RequestParam String key, @RequestParam long timeout) {
        Object value = redisTemplate.opsForList().leftPop(key, timeout, TimeUnit.SECONDS);
        return result("阻塞弹出成功（超时 " + timeout + " 秒）", value);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
