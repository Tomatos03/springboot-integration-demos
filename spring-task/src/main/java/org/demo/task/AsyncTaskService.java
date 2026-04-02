package org.demo.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 异步任务服务类
 *
 * @Async 注解用于将方法标记为异步执行，
 * 调用该方法时会立即返回，实际任务在后台线程中执行。
 *
 * 使用前提：
 *   1. 启动类或配置类添加 @EnableAsync 注解
 *   2. 方法必须是 public 的
 *   3. 方法不能在同一个类中调用（Spring AOP 代理限制）
 *
 * @author Tomatos
 * @date 2025/7/23
 */
@Slf4j
@Service
public class AsyncTaskService {

    /**
     * 场景1：无返回值的异步任务 - 模拟发送邮件
     *
     * 使用 @Async 注解后，调用此方法会立即返回，
     * 实际的邮件发送任务在后台线程池中执行。
     *
     * 适用场景：发送邮件、短信、推送通知等不需要等待结果的操作
     *
     * @param to 收件人邮箱
     */
    @Async("taskExecutor")
    public void sendEmail(String to) {
        log.info("[异步任务-发送邮件] 开始发送邮件给: {}，线程: {}", to, Thread.currentThread().getName());
        // 模拟邮件发送耗时（如连接邮件服务器、发送邮件等）
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[异步任务-发送邮件] 任务被中断");
            return;
        }
        log.info("[异步任务-发送邮件] 邮件发送成功: {}", to);
    }

    /**
     * 场景2：有返回值的异步任务 - 模拟数据处理
     *
     * 返回 CompletableFuture 类型，调用方可以通过：
     *   - future.get() 阻塞等待结果
     *   - future.thenAccept() 异步回调处理结果
     *   - future.completeOnTimeout() 设置超时
     *
     * 适用场景：数据计算、文件处理、接口调用等需要获取结果的操作
     *
     * @return CompletableFuture 包装的处理结果
     */
    @Async("taskExecutor")
    public CompletableFuture<String> processData() {
        log.info("[异步任务-数据处理] 开始处理数据，线程: {}", Thread.currentThread().getName());
        // 模拟数据处理耗时
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[异步任务-数据处理] 任务被中断");
            return CompletableFuture.completedFuture("处理失败");
        }
        String result = "数据处理完成，共处理 1000 条记录";
        log.info("[异步任务-数据处理] {}", result);
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 场景3：批量导入任务 - 模拟分批导入
     *
     * 接收文件名参数，模拟分批导入数据的过程。
     * 使用线程名标识当前执行线程，验证异步执行效果。
     *
     * 适用场景：Excel 导入、CSV 导入、批量数据迁移等
     *
     * @param fileName 要导入的文件名
     */
    @Async("taskExecutor")
    public void batchImport(String fileName) {
        log.info("[异步任务-批量导入] 开始导入文件: {}，线程: {}", fileName, Thread.currentThread().getName());

        // 模拟分批导入，共 5 批
        int totalBatches = 5;
        for (int i = 1; i <= totalBatches; i++) {
            try {
                // 模拟每批处理耗时
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[异步任务-批量导入] 导入被中断，已处理 {}/{} 批", i - 1, totalBatches);
                return;
            }
            log.info("[异步任务-批量导入] 文件: {} - 进度: {}/{} 批", fileName, i, totalBatches);
        }

        log.info("[异步任务-批量导入] 文件: {} - 导入完成", fileName);
    }
}
