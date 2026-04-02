package com.demo.controller;

import com.demo.service.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计数与排行示例接口
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/counter")
@RequiredArgsConstructor
public class CounterRankController {

    private final CounterService counterService;

    // ==================== 阅读量/点赞 ====================

    /**
     * 增加阅读量
     * POST /counter/view?articleId=1001
     */
    @PostMapping("/view")
    public Map<String, Object> incrementView(@RequestParam String articleId) {
        Long count = counterService.incrementView(articleId);
        return result("阅读量 +1", count);
    }

    /**
     * 获取阅读量
     * GET /counter/view?articleId=1001
     */
    @GetMapping("/view")
    public Map<String, Object> getView(@RequestParam String articleId) {
        Long count = counterService.getViewCount(articleId);
        return result("查询阅读量成功", count);
    }

    /**
     * 点赞
     * POST /counter/like?articleId=1001
     */
    @PostMapping("/like")
    public Map<String, Object> incrementLike(@RequestParam String articleId) {
        Long count = counterService.incrementLike(articleId);
        return result("点赞成功", count);
    }

    /**
     * 取消点赞
     * POST /counter/unlike?articleId=1001
     */
    @PostMapping("/unlike")
    public Map<String, Object> decrementLike(@RequestParam String articleId) {
        Long count = counterService.decrementLike(articleId);
        return result("取消点赞成功", count);
    }

    // ==================== 排行榜 ====================

    /**
     * 添加/更新玩家分数
     * POST /counter/ranking?game=chess&player=tomatos&score=1500
     */
    @PostMapping("/ranking")
    public Map<String, Object> addToRanking(@RequestParam String game,
                                            @RequestParam String player,
                                            @RequestParam double score) {
        counterService.addToRanking(game, player, score);
        return result("分数更新成功", score);
    }

    /**
     * 增加分数
     * POST /counter/ranking/increment?game=chess&player=tomatos&delta=10
     */
    @PostMapping("/ranking/increment")
    public Map<String, Object> incrementScore(@RequestParam String game,
                                              @RequestParam String player,
                                              @RequestParam double delta) {
        Double score = counterService.incrementScore(game, player, delta);
        return result("分数增加成功", score);
    }

    /**
     * 获取玩家排名
     * GET /counter/ranking/rank?game=chess&player=tomatos
     */
    @GetMapping("/ranking/rank")
    public Map<String, Object> getRank(@RequestParam String game, @RequestParam String player) {
        Long rank = counterService.getRank(game, player);
        return result("查询排名成功", rank != null ? "第 " + rank + " 名" : "未上榜");
    }

    /**
     * 获取 Top N 排行榜
     * GET /counter/ranking/top?game=chess&n=10
     */
    @GetMapping("/ranking/top")
    public Map<String, Object> getTopN(@RequestParam String game, @RequestParam(defaultValue = "10") int n) {
        List<Map<String, Object>> topList = counterService.getTopN(game, n);
        return result("Top " + n + " 排行榜", topList);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
