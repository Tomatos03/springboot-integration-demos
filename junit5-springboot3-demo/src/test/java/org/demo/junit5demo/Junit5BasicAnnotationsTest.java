package org.demo.junit5demo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 常见基础声明演示：
 * - @DisplayName
 * - @BeforeAll / @AfterAll
 * - @BeforeEach / @AfterEach
 * - @Test
 * - @Nested
 * - @ParameterizedTest + @ValueSource / @CsvSource
 * - @RepeatedTest
 * - @Disabled
 * - 常用断言：assertEquals / assertTrue / assertAll / assertThrows
 *
 */
@DisplayName("JUnit5 基础注解与生命周期演示")
class Junit5BasicAnnotationsTest {

    private StringBuilder state;

    // 整个测试类只执行一次，通常用于初始化“昂贵资源”（连接、容器等）
    @BeforeAll
    static void beforeAll() {
        System.out.println(">>> @BeforeAll：整个测试类开始前执行一次");
    }

    // 整个测试类只执行一次，通常用于释放资源
    @AfterAll
    static void afterAll() {
        System.out.println(">>> @AfterAll：整个测试类结束后执行一次");
    }

    // 每个测试方法执行前都会运行，适合做“测试数据重置”
    @BeforeEach
    void beforeEach(TestInfo testInfo) {
        state = new StringBuilder("init");
        System.out.println(">>> @BeforeEach：开始执行 -> " + testInfo.getDisplayName());
    }

    // 每个测试方法执行后都会运行，可用于清理或打印调试信息
    @AfterEach
    void afterEach(TestInfo testInfo) {
        System.out.println(">>> @AfterEach：结束执行 -> " + testInfo.getDisplayName() + ", state=" + state);
    }

    @Test
    @DisplayName("@Test + assertEquals 示例")
    void shouldAddSuccessfully() {
        int actual = 1 + 2;
        assertEquals(3, actual, "1 + 2 应该等于 3");
        state.append("-added");
    }

    @Test
    @DisplayName("assertAll 聚合断言示例")
    void shouldUseAssertAll() {
        String text = "junit5";
        // assertAll 会执行分组内所有断言，最后统一报告失败项
        assertAll("字符串断言组",
                () -> assertEquals(6, text.length()),
                () -> assertTrue(text.startsWith("ju")),
                () -> assertTrue(text.endsWith("5"))
        );
        state.append("-assertAll");
    }

    @Test
    @DisplayName("assertThrows 异常断言示例")
    void shouldThrowExceptionWhenDividingByZero() {
        // 断言“这段代码必须抛出指定异常类型”
        ArithmeticException ex = assertThrows(
                ArithmeticException.class,
                () -> {
                    int i = 1 / 0;
                    System.out.println(i);
                },
                "除数为 0 时应抛出 ArithmeticException"
        );
        assertTrue(ex.getMessage().contains("/ by zero"));
        state.append("-throws");
    }

    @ParameterizedTest(name = "[{index}] 输入数字 {0} 应为偶数")
    @ValueSource(ints = {2, 4, 6, 8, 10})
    @DisplayName("使用 @ValueSource 演示参数化测试")
    void shouldBeEvenWithValueSource(int number) {
        // 每个输入值都会独立执行一次该测试方法
        assertTrue(number % 2 == 0, "数字应为偶数");
        state.append("-valueSource");
    }

    @ParameterizedTest(name = "[{index}] {0} + {1} = {2}")
    @CsvSource({
            "1, 2, 3",
            "2, 3, 5",
            "10, 20, 30",
            "-1, 1, 0"
    })
    @DisplayName("使用 @CsvSource 演示多参数断言")
    void shouldAddCorrectlyWithCsvSource(int a, int b, int expected) {
        // CsvSource 一行对应一次执行，按参数顺序注入到方法形参
        assertEquals(expected, a + b);
        state.append("-csvSource");
    }

    @RepeatedTest(value = 3, name = "第 {currentRepetition}/{totalRepetitions} 次重复执行")
    @DisplayName("@RepeatedTest 重复测试示例")
    void repeatedTestDemo(RepetitionInfo repetitionInfo) {
        // RepetitionInfo 可拿到当前是第几次、总共执行多少次
        assertTrue(repetitionInfo.getCurrentRepetition() <= repetitionInfo.getTotalRepetitions());
        state.append("-repeat").append(repetitionInfo.getCurrentRepetition());
    }

    @Nested
    @DisplayName("@Nested 嵌套测试示例")
    class NestedCases {

        // 嵌套类也有自己的 @BeforeEach，会在外层 @BeforeEach 之后执行
        @BeforeEach
        void nestedBeforeEach() {
            state.append("-nestedInit");
        }

        @Test
        @DisplayName("嵌套测试用例 A")
        void nestedCaseA() {
            assertTrue(state.toString().contains("nestedInit"));
        }

        @Test
        @DisplayName("嵌套测试用例 B")
        void nestedCaseB() {
            assertFalse(state.isEmpty());
        }
    }

    @Test
    @Disabled
    @DisplayName("@Disabled 示例")
    void disabledDemo() {
        // 被 @Disabled 标记的方法不会执行，常用于临时跳过不稳定用例
    }
}
