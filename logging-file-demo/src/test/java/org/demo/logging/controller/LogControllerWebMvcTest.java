package org.demo.logging.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * LogController 的 Web 切片测试。
 *
 * <p>用 {@code @WebMvcTest} 只加载 Web 层, 不启动完整 Spring 上下文,
 * 验证接口的请求绑定、状态码与响应体是否符合预期。</p>
 *
 * <p>本测试只关心 HTTP 契约, 不关心日志是否落盘(那是 {@code @SpringBootTest}
 * 集成测试的职责)。</p>
 *
 * @author Tomatos
 * @date 2025/9/3
 */
@Tag("webmvc")
@DisplayName("LogController Web 切片测试")
@WebMvcTest(controllers = LogController.class)
class LogControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 校验 GET /log/demo 返回 200 与提示文案。 <p>
     *
     * 五个级别日志都会触发, 此处只关心响应契约。</p>
     *
     * @throws Exception MockMvc 调用异常
     */
    @Test
    @DisplayName("GET /log/demo 返回 200 与提示文案")
    void demoShouldReturnOk() throws Exception {
        mockMvc.perform(get("/log/demo"))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("五个级别")));
    }

    /**
     * 校验 GET /log/order 携带 orderId 参数时返回 200。
     *
     * @throws Exception MockMvc 调用异常
     */
    @Test
    @DisplayName("GET /log/order 带 orderId 参数返回 200")
    void orderShouldReturnOkWithParam() throws Exception {
        mockMvc.perform(get("/log/order").param("orderId", "1001"))
               .andExpect(status().isOk())
               .andExpect(content().string(org.hamcrest.Matchers.containsString("订单处理完成")));
    }

    /**
     * 校验 GET /log/order 不传 orderId 时使用默认值 1001。
     *
     * @throws Exception MockMvc 调用异常
     */
    @Test
    @DisplayName("GET /log/order 不传 orderId 时使用默认值 1001")
    void orderShouldUseDefaultWhenParamMissing() throws Exception {
        mockMvc.perform(get("/log/order"))
               .andExpect(status().isOk())
               .andExpect(content().string("订单处理完成, 请查看日志文件"));
    }

    /**
     * 校验 GET /log/loop 返回 200 并回显条数。 <p>
     *
     * 只验证 HTTP 契约, 不真正输出大批日志。</p>
     *
     * @throws Exception MockMvc 调用异常
     */
    @Test
    @DisplayName("GET /log/loop 返回 200 并回显条数")
    void loopShouldReturnCount() throws Exception {
        mockMvc.perform(get("/log/loop").param("times", "3"))
               .andExpect(status().isOk())
               .andExpect(content().string("已输出 3 条日志, 请观察 logs/ 目录下文件的滚动情况"));
    }
}
