package com.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis ZSet（有序集合）类型操作示例
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/zset")
@RequiredArgsConstructor
public class ZSetController {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 添加元素（带分数） ====================

    /**
     * 添加单个元素
     * POST /zset/zadd?key=ranking&member=tomatos&score=100
     */
    @PostMapping("/zadd")
    public Map<String, Object> zAdd(@RequestParam String key,
                                    @RequestParam String member,
                                    @RequestParam double score) {
        Boolean success = redisTemplate.opsForZSet().add(key, member, score);
        return result("添加元素" + (success ? "成功" : "已存在，分数已更新"), success);
    }

    /**
     * 批量添加元素
     * POST /zset/zadd-batch?key=ranking
     * Body: {"tomatos":100,"jack":85,"alice":92}
     */
    @PostMapping("/zadd-batch")
    public Map<String, Object> zAddBatch(@RequestParam String key,
                                         @RequestBody Map<String, Double> members) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = new java.util.HashSet<>();
        members.forEach((member, score) ->
                tuples.add(ZSetOperations.TypedTuple.of(member, score)));
        Long count = redisTemplate.opsForZSet().add(key, tuples);
        return result("批量添加 " + count + " 个元素", count);
    }

    // ==================== 获取排名 ====================

    /**
     * 获取排名（从 0 开始，分数从低到高）
     * GET /zset/zrank?key=ranking&member=tomatos
     */
    @GetMapping("/zrank")
    public Map<String, Object> zRank(@RequestParam String key, @RequestParam String member) {
        Long rank = redisTemplate.opsForZSet().rank(key, member);
        return result("排名（从低到高）", rank);
    }

    /**
     * 获取倒序排名（分数从高到低）
     * GET /zset/zrevrank?key=ranking&member=tomatos
     */
    @GetMapping("/zrevrank")
    public Map<String, Object> zRevRank(@RequestParam String key, @RequestParam String member) {
        Long rank = redisTemplate.opsForZSet().reverseRank(key, member);
        return result("排名（从高到低）", rank);
    }

    /**
     * 获取分数
     * GET /zset/zscore?key=ranking&member=tomatos
     */
    @GetMapping("/zscore")
    public Map<String, Object> zScore(@RequestParam String key, @RequestParam String member) {
        Double score = redisTemplate.opsForZSet().score(key, member);
        return result("查询分数成功", score);
    }

    // ==================== 获取范围元素 ====================

    /**
     * 按排名范围获取（从低到高）
     * GET /zset/zrange?key=ranking&start=0&end=2
     */
    @GetMapping("/zrange")
    public Map<String, Object> zRange(@RequestParam String key,
                                      @RequestParam long start,
                                      @RequestParam long end) {
        Set<Object> members = redisTemplate.opsForZSet().range(key, start, end);
        return result("范围查询成功（从低到高）", members);
    }

    /**
     * 按排名范围获取（从高到低）
     * GET /zset/zrevrange?key=ranking&start=0&end=2
     */
    @GetMapping("/zrevrange")
    public Map<String, Object> zRevRange(@RequestParam String key,
                                         @RequestParam long start,
                                         @RequestParam long end) {
        Set<Object> members = redisTemplate.opsForZSet().reverseRange(key, start, end);
        return result("范围查询成功（从高到低）", members);
    }

    // ==================== 按分数范围查询 ====================

    /**
     * 按分数范围查询
     * GET /zset/zrangebyscore?key=ranking&min=80&max=100
     */
    @GetMapping("/zrangebyscore")
    public Map<String, Object> zRangeByScore(@RequestParam String key,
                                             @RequestParam double min,
                                             @RequestParam double max) {
        Set<Object> members = redisTemplate.opsForZSet().rangeByScore(key, min, max);
        return result("分数范围查询成功", members);
    }

    /**
     * 按分数范围查询带分数
     * GET /zset/zrangebyscore-withscore?key=ranking&min=80&max=100
     */
    @GetMapping("/zrangebyscore-withscore")
    public Map<String, Object> zRangeByScoreWithScore(@RequestParam String key,
                                                      @RequestParam double min,
                                                      @RequestParam double max) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
        Map<Object, Object> result = new java.util.LinkedHashMap<>();
        if (tuples != null) {
            tuples.forEach(t -> result.put(t.getValue(), t.getScore()));
        }
        return result("分数范围查询（带分数）成功", result);
    }

    /**
     * 获取集合大小
     * GET /zset/zcard?key=ranking
     */
    @GetMapping("/zcard")
    public Map<String, Object> zCard(@RequestParam String key) {
        Long size = redisTemplate.opsForZSet().zCard(key);
        return result("集合大小", size);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
