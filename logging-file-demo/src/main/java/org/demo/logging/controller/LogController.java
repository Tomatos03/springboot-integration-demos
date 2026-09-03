package org.demo.logging.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志演示控制器。
 *
 * <p>提供一组接口, 分别触发不同级别与不同来源的日志, 便于观察日志文件的内容与滚动策略。</p>
 *
 * @author Tomatos
 * @date 2025/9/3
 */
@Slf4j
@RestController
@RequestMapping("/log")
public class LogController {

    /**
     * 一次性输出 TRACE ~ ERROR 五个级别的日志。 <p>
     *
     * 访问:{@code GET /log/demo}。</p> <p>
     * 默认 root 级别为 INFO, 因此 TRACE / DEBUG 不会出现在日志文件里; 想观察这两个级别, 可在 application.yml 中临时调低根日志级别。</p>
     *
     * @return 演示结果
     */
    @GetMapping("/demo")
    public String demo() {
        log.trace("TRACE 级别日志: 最详细的调试信息");
        log.debug("DEBUG 级别日志: 定位问题的辅助信息");
        log.info("INFO 级别日志: 业务关键节点信息");
        log.warn("WARN 级别日志: 潜在风险提示");
        log.error("ERROR 级别日志: 异常与错误信息", new RuntimeException("演示异常"));
        return "已输出五个级别的日志, 请查看控制台与日志文件 logs/app.log";
    }

    /**
     * 模拟一次订单处理流程。 <p>
     *
     * 访问:{@code GET /log/order?orderId=1001}。</p> <p>
     * 体会占位符用法, 避免手工拼接字符串; 生产环境建议把链路标识(如 traceId)带进日志, 便于在文件中检索。</p>
     *
     * @param orderId 订单号
     * @return 处理结果
     */
    @GetMapping("/order")
    public String order(@RequestParam(defaultValue = "1001") String orderId) {
        log.info("==> 订单处理开始, orderId={}", orderId);
        log.debug("查询订单详情, orderId={}", orderId);
        log.info("扣减库存成功, orderId={}", orderId);
        log.info("<== 订单处理结束, orderId={}", orderId);
        return "订单处理完成, 请查看日志文件";
    }

    /**
     * 触发大规模日志输出, 用于观察滚动策略。 <p>
     *
     * 访问:{@code GET /log/loop?times=1000}。</p> <p>
     * 当 app.log 达到 max-file-size 时会被归档, 命名形如 app.log.2025-09-03.0.log.gz; 最新日志仍写入 app.log, 归档数量受
     * max-history 控制。</p>
     *
     * @param times 循环次数
     * @return 执行结果
     */
    @GetMapping("/loop")
    public String loop(@RequestParam(defaultValue = "100000") long times) {
        log.info("开始批量输出日志, times={}", times);
        for (long i = 0; i < times; i++) {
            log.info("批量日志第 {} 条, msg={}", i, "这是一条用于撑爆日志文件的测试内容");
        }
        log.info("批量输出日志完成, times={}", times);
        return "已输出 " + times + " 条日志, 请观察 logs/ 目录下文件的滚动情况";
    }
}
