package com.demo.controller;

import com.demo.service.DelayTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 延迟任务示例接口
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@RestController
@RequestMapping("/delay")
@RequiredArgsConstructor
public class DelayTaskController {

    private final DelayTaskService delayTaskService;

    /**
     * 创建订单（自动加入延迟队列）
     * POST /delay/order?userId=1&amount=99.9&timeoutMinutes=5
     */
    @PostMapping("/order")
    public Map<String, Object> createOrder(@RequestParam Long userId,
                                           @RequestParam BigDecimal amount,
                                           @RequestParam(defaultValue = "5") int timeoutMinutes) {
        String orderId = delayTaskService.createOrder(userId, amount, timeoutMinutes);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("timeoutMinutes", timeoutMinutes);
        return result("订单创建成功", data);
    }

    /**
     * 查询订单信息
     * GET /delay/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public Map<String, Object> getOrder(@PathVariable String orderId) {
        return result("查询成功", delayTaskService.getOrder(orderId));
    }

    /**
     * 支付订单
     * POST /delay/order/{orderId}/pay
     */
    @PostMapping("/order/{orderId}/pay")
    public Map<String, Object> payOrder(@PathVariable String orderId) {
        boolean success = delayTaskService.payOrder(orderId);
        return result(success ? "支付成功" : "支付失败", success);
    }

    /**
     * 手动处理超时订单
     * POST /delay/process
     */
    @PostMapping("/process")
    public Map<String, Object> processExpired() {
        String result = delayTaskService.processExpiredOrders();
        return result(result, null);
    }

    /**
     * 查看延迟队列剩余任务数
     * GET /delay/queue-size
     */
    @GetMapping("/queue-size")
    public Map<String, Object> getQueueSize() {
        Long size = delayTaskService.getQueueSize();
        return result("延迟队列剩余任务数", size);
    }

    private Map<String, Object> result(String message, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("message", message);
        map.put("data", data);
        return map;
    }
}
