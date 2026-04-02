package com.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 计数与排行服务
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CounterService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 阅读量/点赞计数 ====================

    /**
     * 增加阅读量
     */
    public Long incrementView(String articleId) {
        String key = "counter:view:" + articleId;
        Long count = redisTemplate.opsForValue().increment(key);
        log.info("文章 {} 阅读量: {}", articleId, count);
        return count;
    }

    /**
     * 获取阅读量
     */
    public Long getViewCount(String articleId) {
        Object count = redisTemplate.opsForValue().get("counter:view:" + articleId);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    /**
     * 点赞
     */
    public Long incrementLike(String articleId) {
        return redisTemplate.opsForValue().increment("counter:like:" + articleId);
    }

    /**
     * 取消点赞
     */
    public Long decrementLike(String articleId) {
        return redisTemplate.opsForValue().decrement("counter:like:" + articleId);
    }

    /**
     * 获取点赞数
     */
    public Long getLikeCount(String articleId) {
        Object count = redisTemplate.opsForValue().get("counter:like:" + articleId);
        return count != null ? Long.valueOf(count.toString()) : 0L;
    }

    // ==================== 实时排行榜 ====================

    /**
     * 添加/更新分数
     */
    public Boolean addToRanking(String rankingKey, String member, double score) {
        return redisTemplate.opsForZSet().add("ranking:" + rankingKey, member, score);
    }

    /**
     * 增加分数
     */
    public Double incrementScore(String rankingKey, String member, double delta) {
        return redisTemplate.opsForZSet().incrementScore("ranking:" + rankingKey, member, delta);
    }

    /**
     * 获取排名（从高到低，0 为第一名）
     */
    public Long getRank(String rankingKey, String member) {
        Long rank = redisTemplate.opsForZSet().reverseRank("ranking:" + rankingKey, member);
        // +1 使其从 1 开始
        return rank != null ? rank + 1 : null;
    }

    /**
     * 获取 Top N
     */
    public List<Map<String, Object>> getTopN(String rankingKey, int n) {
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores("ranking:" + rankingKey, 0, n - 1);
        List<Map<String, Object>> result = new ArrayList<>();
        if (tuples != null) {
            int rank = 1;
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("rank", rank++);
                item.put("member", tuple.getValue());
                item.put("score", tuple.getScore());
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 获取分数
     */
    public Double getScore(String rankingKey, String member) {
        return redisTemplate.opsForZSet().score("ranking:" + rankingKey, member);
    }
}
