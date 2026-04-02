package org.demo.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 定时任务示例类
 *
 * Spring Task 提供了 @Scheduled 注解来实现定时任务调度，
 * 支持以下几种调度方式：
 *   1. fixedRate - 固定频率执行（不等待上次完成）
 *   2. fixedDelay - 固定延迟执行（等待上次完成后延迟指定时间）
 *   3. cron - 使用 Cron 表达式自定义调度规则
 *   4. initialDelay - 首次延迟执行
 *
 * @author Tomatos
 * @date 2025/7/23
 */
@Slf4j
@Component
public class ScheduledTask {

    /**
     * 场景1：固定频率执行 - 模拟数据同步
     *
     * fixedRate = 5000 表示每隔 5 秒执行一次（从上一次开始时间算起）
     * 注意：如果任务执行时间超过间隔时间，下一个任务会在上一个任务完成后立即执行
     *
     * 应用场景：定时从外部系统拉取数据、定时刷新缓存等
     */
    @Scheduled(fixedRate = 5000)
    public void syncData() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("[定时任务-数据同步] 开始同步数据，执行时间: {}", now);
        // 模拟同步耗时
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[定时任务-数据同步] 数据同步完成");
    }

    /**
     * 场景2：Cron 表达式执行 - 模拟清理过期数据
     *
     * cron = "0 0 2 * * ?" 表示每天凌晨 2 点执行
     * Cron 表达式格式：秒 分 时 日 月 周 [年]
     *
     * 常用 Cron 表达式示例：
     *   - "0 0/5 * * * ?"    每5分钟执行一次
     *   - "0 0 12 * * ?"     每天中午12点执行
     *   - "0 0 10,14,16 * * ?" 每天上午10点、下午2点、4点执行
     *   - "0 0/30 9-17 * * ?" 朝九晚五内每半小时执行
     *
     * 应用场景：定时清理日志、定时备份数据、定时生成报表等
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("[定时任务-清理过期数据] 开始清理，执行时间: {}", now);
        // 模拟清理逻辑
        log.info("[定时任务-清理过期数据] 清理完成，共清理 100 条过期记录");
    }

    /**
     * 场景3：固定延迟执行 - 模拟统计报表
     *
     * fixedDelay = 10000 表示上一次任务执行完毕后，等待 10 秒再执行下一次
     * 与 fixedRate 的区别：
     *   - fixedRate: 不管上次任务是否完成，到时间就执行
     *   - fixedDelay: 等上次任务完成后，再等待指定时间才执行
     *
     * 应用场景：对执行时间不固定的任务，如生成报表、处理队列消息等
     */
    @Scheduled(fixedDelay = 10000)
    public void generateReport() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("[定时任务-统计报表] 开始生成报表，执行时间: {}", now);
        // 模拟报表生成耗时
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("[定时任务-统计报表] 报表生成完成");
    }

    /**
     * 场景4：延迟启动执行 - 模拟健康检查
     *
     * initialDelay = 10000 表示应用启动后延迟 10 秒才开始执行
     * 配合 fixedRate = 5000 使用，表示首次延迟 10 秒后，每隔 5 秒执行一次
     *
     * 应用场景：等待应用初始化完成后再执行的任务，如等待其他服务启动、
     *          等待缓存预热完成等
     */
    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    public void healthCheck() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("[定时任务-健康检查] 系统健康检查，执行时间: {}", now);
        // 模拟健康检查逻辑
        log.info("[定时任务-健康检查] 系统状态正常");
    }
}
