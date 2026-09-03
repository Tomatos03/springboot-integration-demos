package org.demo.logging;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.demo.logging.controller.LogController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 日志持久化集成测试(方式一: application.yml 纯属性配置)。
 *
 * <p>启动完整 Spring Boot 上下文, 校验:
 * 1) HTTP 接口端到端可用;
 * 2) 日志真的写入了由 {@code logging.file.name} 指定的文件;
 * 3) 默认 INFO 级别下, INFO/WARN/ERROR 落盘、TRACE/DEBUG 被过滤。</p>
 *
 * <p><b>关键机制:</b> {@code logging.file.name} 在 Spring Environment 就绪之前
 * (Logback 初始化阶段)就被读取, 因此 {@code @TestPropertySource} /
 * {@code @DynamicPropertySource} 都无法影响它, 只能通过系统属性或启动参数提前注入。
 * 本模块在 pom.xml 的 surefire 插件里用 {@code systemPropertyVariables} 把日志文件
 * 指到 {@code target/test-logs/app.log}(被 gitignore), 避免污染仓库工作区。</p>
 *
 * <p>注意: 测试里不要删除该日志文件——Logback 的文件 appender 会一直持有其句柄,
 * 删除后写入会进入孤儿 inode, 导致文件在磁盘上不可见。这里只追加断言内容,
 * 目录由 target/ 的清理机制(gitignore / clean)负责。</p>
 *
 * @author Tomatos
 * @date 2025/9/3
 */
@Tag("springboot")
@DisplayName("application.yml 属性方式集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogConfigFileIntegrationTest {

    /** 与 surefire 系统属性一致的路径(相对 Maven 模块工作目录 = 模块根) */
    private static final Path LOG_FILE = Paths.get("target/test-logs/app.log").toAbsolutePath();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LogController logController;

    /**
     * 校验 HTTP /log/demo 返回 200。 <p>
     *
     * 通过 TestRestTemplate 发起真实 HTTP 请求断言状态码。</p>
     */
    @Test
    @DisplayName("HTTP /log/demo 返回 200")
    void demoEndpointReturnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/log/demo", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * 校验 HTTP /log/order 携带参数返回 200。 <p>
     *
     * 通过 TestRestTemplate 发起真实 HTTP 请求并带查询参数。</p>
     */
    @Test
    @DisplayName("HTTP /log/order 带参数返回 200")
    void orderEndpointReturnsOk() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/log/order?orderId=9527",
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * 校验调用 /log/demo 后日志文件真实存在且有内容。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    @DisplayName("调用 /log/demo 后日志文件真实存在且有内容")
    void logFileIsCreatedWhenEndpointHit() throws Exception {
        restTemplate.getForEntity("/log/demo", String.class);
        assertTrue(Files.exists(LOG_FILE), "日志文件应被创建: " + LOG_FILE);
        assertTrue(Files.size(LOG_FILE) > 0, "日志文件不应为空");
    }

    /**
     * 校验默认 INFO 级别下 INFO/WARN/ERROR 落盘、TRACE/DEBUG 被过滤。 <p>
     *
     * 直接读取日志文件内容断言级别过滤行为。</p>
     *
     * @throws Exception 文件读取异常
     */
    @Test
    @DisplayName("默认 INFO 级别下 INFO/WARN/ERROR 落盘、TRACE/DEBUG 被过滤")
    void levelFilteringWorksOnFile() throws Exception {
        restTemplate.getForEntity("/log/demo", String.class);
        String content = Files.readString(LOG_FILE);
        // 落盘: INFO/WARN/ERROR 各至少一条(ERROR 日志带异常堆栈)
        assertTrue(content.contains("INFO 级别日志"), "预计 INFO 落盘");
        assertTrue(content.contains("WARN 级别日志"), "预计 WARN 落盘");
        assertTrue(content.contains("ERROR 级别日志"), "预计 ERROR 落盘");
        // 过滤: 根级别 INFO 下 TRACE/DEBUG 不应出现在文件
        assertTrue(!content.contains("TRACE 级别日志"), "TRACE 不应落盘");
        assertTrue(!content.contains("DEBUG 级别日志"), "DEBUG 不应落盘");
    }

    /**
     * 校验直接调用 Controller 也会触发日志落盘。 <p>
     *
     * 不经过 HTTP 层, 验证日志来自 Controller 自身的 {@code @Slf4j} 输出。</p>
     *
     * @throws Exception 文件读取异常
     */
    @Test
    @DisplayName("直接用 LogController 触发日志也会写文件(不经 HTTP)")
    void directControllerLogsToFile() throws Exception {
        logController.demo();
        assertTrue(Files.exists(LOG_FILE), "直接调用 Controller 触发日志也应落盘");
    }
}
