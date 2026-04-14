package org.demo.junit5demo;

import org.demo.junit5demo.service.CalculatorService;
import org.demo.junit5demo.service.GreetingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 该注解用于加载SpringBoot中定义的Controller、Filter定义的bean
@WebMvcTest(controllers = org.demo.junit5demo.controller.DemoController.class)
// 该注解用于配置测试环境中的属性值，覆盖默认的 application.properties 配置。
@TestPropertySource(properties = {
        "spring.main.banner-mode=off",
        "customs.demoTestValue=zhou"
})
@DisplayName("DemoController测试")
@Tag("webmvc")
class DemoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @MockitoBean
    private GreetingService greetingService;

    @MockitoBean
    private CalculatorService calculatorService;

    @Nested
    @DisplayName("测试 GET /api/hello")
    class HelloApiTests {
        // /api/hello 会调用 GreetingService.greet(...)，所以这里需要为 mock service 显式定义期望行为。
        @Test
        @DisplayName("传入 name 时，返回 GreetingService 的结果")
        void shouldReturnGreetingFromServiceWhenNameProvided() throws Exception {
            given(greetingService.greet(eq("Tom"))).willReturn("Hello, Tom!");

            mockMvc.perform(get("/api/hello").param("name", "Tom"))
                   .andExpect(status().isOk())
                   .andExpect(content().string("Hello, Tom!"));
        }

        @Test
        @DisplayName("未传 name 时，使用默认值 JUnit5")
        void shouldUseDefaultNameWhenParameterMissing() throws Exception {
            given(greetingService.greet(eq("JUnit5"))).willReturn("Hello, JUnit5!");

            mockMvc.perform(get("/api/hello"))
                   .andExpect(status().isOk())
                   .andExpect(content().string("Hello, JUnit5!"));
        }
    }

    @Nested
    @DisplayName("GET /api/sum")
    class SumApiTests {
        // /api/sum 的计算逻辑在 controller 内部完成，不依赖 GreetingService，
        // 因此这组测试只需断言请求参数与响应结果，不需要额外设置 mock 行为。

        @Test
        @DisplayName("两个整数求和返回 200 和正确结果")
        void shouldReturnSumForValidParameters() throws Exception {
            mockMvc.perform(
                           get("/api/sum")
                                   .param("a", "3")
                                   .param("b", "5")
                   )
                   .andExpect(status().isOk())
                   .andExpect(content().string("8"));
        }

        @Test
        @DisplayName("缺少参数时返回 400")
        void shouldReturnBadRequestWhenParameterMissing() throws Exception {
            mockMvc.perform(
                           get("/api/sum").param("a", "3")
                   )
                   .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get /is-event")
    class EvenApiTest {

        @DisplayName("偶数判断测试")
        @Test
        public void shouldIsEvent() throws Exception {
            // 定义两组service对应的isEven方法执行后的预期值
            // 这里除了4 和 12 其余值都返回默认值false
            given(calculatorService.isEven(eq(4))).willReturn(true);
            given(calculatorService.isEven(eq(12))).willReturn(true);

            mockMvc.perform(
                           get("/is-even/12")
                   )
                   .andExpect(status().isOk())
                   .andExpect(content().string("true"));
        }

        @DisplayName("当 service 抛出异常时，使用 assertThrows 断言")
        @Test
        void shouldThrowWhenCalculatorServiceFails() {
            given(calculatorService.isEven(eq(-1)))
                    .willThrow(new IllegalArgumentException("number must be >= 0"));

            Exception ex = Assertions.assertThrows(
                    Exception.class,
                    () -> mockMvc.perform(get("/is-even/-1")).andReturn()
            );

            Throwable rootCause = ex.getCause() == null ? ex : ex.getCause();
            Assertions.assertTrue(rootCause instanceof IllegalArgumentException);
            Assertions.assertEquals("number must be >= 0", rootCause.getMessage());
        }
    }

    @Test
    @DisplayName("直接读取自定义属性 customs.demoTestValue")
    void shouldReadCustomProperty() {
        // 读取@TestPropertyResource中定义的属性
        Assertions.assertEquals(
                "zhou",
                environment.getProperty("customs.demoTestValue")
        );
    }
}
