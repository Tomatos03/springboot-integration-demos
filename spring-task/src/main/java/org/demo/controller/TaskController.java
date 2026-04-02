package org.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.task.AsyncTaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 异步任务控制器
 *
 * 提供 REST 接口用于触发异步任务，
 * 可以通过 Postman 或浏览器调用这些接口来验证异步执行效果。
 *
 * 验证要点：
 *   1. 调用接口后应立即返回，不会阻塞
 *   2. 查看控制台日志，确认任务在后台线程中执行
 *   3. 线程名应为 "async-task-x" 格式
 *
 * @author Tomatos
 * @date 2025/7/23
 */
@Slf4j
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final AsyncTaskService asyncTaskService;

    /**
     * 触发发送邮件任务
     *
     * 测试方式：
     *   POST http://localhost:8080/task/send-email
     *   Body: {"to": "test@example.com"}
     *
     * 预期结果：
     *   - 接口立即返回响应
     *   - 控制台显示邮件发送的异步日志
     *
     * @param params 请求参数，包含收件人邮箱
     * @return 响应结果
     */
    @PostMapping("/send-email")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody Map<String, String> params) {
        String to = params.getOrDefault("to", "default@example.com");
        log.info("[Controller] 接收到发送邮件请求，收件人: {}", to);

        // 调用异步方法，会立即返回
        asyncTaskService.sendEmail(to);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "邮件发送任务已提交，请查看控制台日志");
        return ResponseEntity.ok(result);
    }

    /**
     * 触发数据处理任务（带返回值）
     *
     * 测试方式：
     *   POST http://localhost:8080/task/process-data
     *
     * 预期结果：
     *   - 接口立即返回 "任务已提交"
     *   - 约 5 秒后控制台显示处理结果
     *
     * 注意：
     *   由于返回 CompletableFuture，如果想获取异步结果，
     *   可以通过轮询或 WebSocket 的方式通知前端
     *
     * @return 响应结果
     */
    @PostMapping("/process-data")
    public ResponseEntity<Map<String, Object>> processData() {
        log.info("[Controller] 接收到数据处理请求");

        // 调用异步方法，返回 CompletableFuture
        CompletableFuture<String> future = asyncTaskService.processData();

        // 方式1：添加回调，异步处理结果
        future.thenAccept(result -> {
            log.info("[Controller] 数据处理完成，结果: {}", result);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "数据处理任务已提交，约 5 秒后完成");
        return ResponseEntity.ok(result);
    }

    /**
     * 触发批量导入任务
     *
     * 测试方式：
     *   POST http://localhost:8080/task/batch-import
     *   Body: {"fileName": "data.xlsx"}
     *
     * 预期结果：
     *   - 接口立即返回响应
     *   - 控制台每秒打印导入进度
     *
     * @param params 请求参数，包含文件名
     * @return 响应结果
     */
    @PostMapping("/batch-import")
    public ResponseEntity<Map<String, Object>> batchImport(@RequestBody Map<String, String> params) {
        String fileName = params.getOrDefault("fileName", "default.xlsx");
        log.info("[Controller] 接收到批量导入请求，文件: {}", fileName);

        // 调用异步方法，会立即返回
        asyncTaskService.batchImport(fileName);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "批量导入任务已提交，文件: " + fileName);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量触发多个异步任务
     *
     * 测试方式：
     *   POST http://localhost:8080/task/batch-trigger
     *
     * 预期结果：
     *   - 接口立即返回响应
     *   - 控制台显示多个任务并行执行的日志
     *   - 线程名不同，说明在不同线程中执行
     *
     * @return 响应结果
     */
    @PostMapping("/batch-trigger")
    public ResponseEntity<Map<String, Object>> batchTrigger() {
        log.info("[Controller] 批量触发异步任务");

        // 同时触发多个异步任务
        asyncTaskService.sendEmail("user1@example.com");
        asyncTaskService.sendEmail("user2@example.com");
        asyncTaskService.sendEmail("user3@example.com");
        asyncTaskService.processData();
        asyncTaskService.batchImport("orders.xlsx");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "已触发 5 个异步任务，请查看控制台日志");
        return ResponseEntity.ok(result);
    }
}
