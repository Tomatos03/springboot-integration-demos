package org.demo.logging;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.demo.logging.controller.LogController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日志持久化集成测试(debug profile)。
 *
 * <p>通过 {@code @ActiveProfiles("debug")} 激活 application.yml 里的 debug profile,
 * 该 profile 把 {@code org.demo.logging.controller} 包级别调为 DEBUG, 验证:
 * 1) DEBUG 级别的日志真的写入了文件;
 * 2) 控制台(通过 OutputCapture 捕获 stdout/stderr)也能看到 DEBUG 日志。</p>
 *
 * <p>日志文件路径同样来自 pom.xml 的 surefire 系统属性(见
 * {@code LogConfigFileIntegrationTest} 的说明), 本类复用同一路径。</p>
 *
 * @author Tomatos
 * @date 2025/9/3
 */
@Tag("springboot")
@ActiveProfiles("debug")
@DisplayName("debug profile 集成测试")
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class DebugProfileIntegrationTest {

    private static final Path LOG_FILE = Paths.get("target/test-logs/app.log").toAbsolutePath();

    @Autowired
    private LogController logController;

    /**
     * 校验 debug profile 下 DEBUG 级别的日志写入文件。 <p>
     *
     * debug profile 会把 controller 包级别调为 DEBUG, 因此 DEBUG 日志应落盘。</p>
     *
     * @throws Exception 文件读取异常
     */
    @Test
    @DisplayName("debug profile 下 DEBUG 日志写入文件")
    void debugLogPersistsToFile() throws Exception {
        logController.order("1001");
        assertThat(LOG_FILE).exists();
        assertThat(Files.readString(LOG_FILE))
                .contains("查询订单详情, orderId=1001")   // 这一条是 DEBUG 级别
                .contains("==> 订单处理开始, orderId=1001");
    }

    /**
     * 校验 debug profile 下控制台能看到 DEBUG 级别的日志。 <p>
     *
     * 通过 {@code OutputCaptureExtension} 捕获进程输出并断言。</p>
     *
     * @param output 捕获到的进程输出
     */
    @Test
    @DisplayName("debug profile 下控制台出现 DEBUG 日志")
    void debugLogsAppearOnConsole(CapturedOutput output) {
        logController.order("1001");
        // OutputCaptureExtension 捕获运行线程 + 系统输出的内容
        assertThat(output.getAll()).contains("DEBUG")
                                   .contains("查询订单详情, orderId=1001");
    }
}
