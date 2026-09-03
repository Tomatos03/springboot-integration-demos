package org.demo.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 日志持久化 demo 启动类
 *
 * <p>启动后访问:{@code GET http://localhost:8080/log/demo} 触发演示日志,
 * 日志既会输出到控制台, 也会按 application.yml 中的配置写入文件。</p>
 *
 * @author Tomatos
 * @date 2025/9/3
 */
@Slf4j
@SpringBootApplication
public class LoggingFileDemoApplication {

    /**
     * 应用入口。 <p>
     *
     * 启动前与启动后各输出一条日志, 用于验证日志持久化配置是否生效。</p>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        // 启动时输出一条日志, 验证文件日志配置是否生效
        log.info("LoggingFileDemoApplication 正在启动...");
        SpringApplication.run(LoggingFileDemoApplication.class, args);
        log.info("LoggingFileDemoApplication 启动完成");
    }
}
