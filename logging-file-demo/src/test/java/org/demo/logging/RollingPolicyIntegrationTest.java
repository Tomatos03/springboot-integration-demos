package org.demo.logging;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日志滚动策略集成测试。
 *
 * <p>验证「文件达到指定大小后会发生什么」这一核心问题:</p>
 * <ul>
 *     <li>单个文件达到 {@code max-file-size} 后触发滚动, 原文件被归档(带日期/序号/压缩);</li>
 *     <li>滚动后仍会创建一个新的当前日志文件, 后续日志继续写入;</li>
 *     <li>归档不会无限增长: 超过 {@code total-size-cap} 上限时, 最旧的归档被清理。</li>
 * </ul>
 *
 * <p><b>关于 max-history:</b> {@code SizeAndTimeBasedRollingPolicy} 的
 * {@code max-history} 按「日期窗口」清理归档 —— 只保留最近 N 天的归档; 同一天内因
 * {@code max-file-size} 滚动出的多个归档(序号 .0/.1/...)不属于过期窗口, 不受它约束。
 * 测试运行在一天之内、无法等日期推进, 因此改用 {@code total-size-cap}(按总大小删除
 * 最旧归档, 当日即生效)来确定性验证「归档不会无限增长」这一保留行为。</p>
 *
 * <p>通过 {@code @ActiveProfiles("rolling")} 激活 {@code application-rolling.yml},
 * 把 {@code max-file-size} 调到 1KB、{@code total-size-cap} 调到 6KB, 少量日志即可确定性触发
 * 滚动, 测试快速且稳定。日志文件路径来自 surefire 注入的
 * {@code target/test-logs/app.log}(见 pom.xml)。</p>
 *
 * <p><b>注意:</b> 本类不删除正在写入的 {@code app.log}(Logback 持有其句柄);
 * 只读取归档文件作断言, 目录由 target/ 的 gitignore / clean 机制负责。</p>
 *
 * @author Tomatos
 * @date 2025/9/3
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("rolling")
@DisplayName("日志滚动策略集成测试")
@Tag("springboot")
class RollingPolicyIntegrationTest {

    /** 与 surefire 系统属性一致的当前日志文件路径 */
    private static final Path LOG_FILE = Paths.get("target/test-logs/app.log").toAbsolutePath();

    /** 触发日志吞吐, 让当前文件快速跨过 max-file-size 阈值 */
    @Test
    @DisplayName("单个文件达到 max-file-size 后滚动出归档文件")
    void fileRollsArchiveAfterReachingMaxSize() throws Exception {
        // 不断刷日志, 直到当前日志文件触发滚动
        writeEnoughToForceRoll();

        Path dir = LOG_FILE.getParent();
        assertThat(dir).isNotNull();

        // 当前日志文件应仍在(滚动后新建), 且至少存在一个归档文件
        assertThat(LOG_FILE).exists();
        List<Path> archives = listArchives(dir);
        assertThat(archives).isNotEmpty();

        // 归档命名符合 pattern: app.log.2025-09-03.0.log.gz
        assertThat(archives.get(0).getFileName().toString())
                .startsWith("app.log.")
                .endsWith(".gz");
    }

    /**
     * 验证归档不会无限增长: 超过 {@code total-size-cap} 后最旧归档被清理。
     *
     * <p>profile 里 {@code total-size-cap=6KB}。归档 gzip 压缩后落盘(每个通常只有约 200B),
     * 因此 6KB 上限对应的归档数约 20~30 个; 总大小一超上限, Logback 就在后续滚动时删除
     * 最旧的归档, 把总量压在上限附近、不随写入量线性膨胀。
     * 若上限未生效, 本方法会留下数百个归档, 总大小断言必然失败。</p>
     */
    @Test
    @DisplayName("归档总大小受 total-size-cap 约束, 超过上限的最旧归档被清理")
    void archivesAreBoundedByTotalSizeCap() throws Exception {
        writeEnoughToForceRoll();

        Path dir = LOG_FILE.getParent();
        assertThat(dir).isNotNull();

        List<Path> archives = listArchives(dir);
        // 有滚动发生, 且归档命名符合 pattern: app.log.2025-09-03.0.log.gz
        assertThat(archives).isNotEmpty();
        for (Path archive : archives) {
            assertThat(archive.getFileName().toString())
                    .startsWith("app.log.")
                    .endsWith(".gz");
        }

        // total-size-cap=6KB: 归档总大小有界(放宽到 24KB 以吸收归档大小/清理时序的边界抖动);
        // 若 total-size-cap 未生效, 数百个归档的总大小远超该上界
        long totalBytes = archives.stream().mapToLong(p -> {
            try {
                return Files.size(p);
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }).sum();
        assertThat(totalBytes).isLessThanOrEqualTo(24L * 1024);
    }

    /** 当前日志文件在滚动后继续写入, 内容非空且仍为“活动文件” */
    @Test
    @DisplayName("滚动后当前日志文件继续写入新内容")
    void activeFileKeepsGrowingAfterRoll() throws Exception {
        writeEnoughToForceRoll();
        // 滚动后再写一批, 当前文件应仍然存在且有内容
        log.info("滚动后再写入一条验证日志: {}", System.nanoTime());
        assertThat(LOG_FILE).exists();
        assertThat((int) Files.size(LOG_FILE)).isPositive();
    }

    // ------------------------------------------------------------------
    // 辅助方法
    // ------------------------------------------------------------------

    /** 持续写日志直到触发多次滚动; 写完留一点时间让 Logback 落盘/清理, 供文件系统观察 */
    private void writeEnoughToForceRoll() {
        // 单条约 200 字节, max-file-size=1KB, 写 2000 条必然跨过大量滚动阈值;
        // 同时避免无线膨胀导致测试过慢。
        for (int i = 0; i < 2000; i++) {
            log.info("滚动测试占位日志第 {} 条, 填充文本 padding padding padding padding padding", i);
        }
        // 给 Logback 异步刷新留一点时间, 让文件名变更落盘可见
        awaitSync();
    }

    /** 简单等待, 让文件系统上的归档可见(Logback 滚动为同步原子操作, 这里主要等服务端刷新) */
    private void awaitSync() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 列出日志目录下所有归档文件(形如 app.log.*.log.gz), 按文件名排序 */
    private static List<Path> listArchives(Path dir) throws Exception {
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().startsWith("app.log."))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
}