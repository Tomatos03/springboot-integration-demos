# junit5-springboot3-demo

一个用于学习 Spring Boot 3 + JUnit 5 常见测试写法的独立模块。
重点是区分场景：什么时候写纯单测，什么时候写 MVC 切片测试，什么时候写集成测试。

## 三种测试环境定义

### 1) 纯单元测试（不启动 Spring 容器）

- 定义：只测试一个类或方法，依赖通过 mock 或手工构造
- 特点：速度最快、定位最精确、反馈最直接
- 适用：`CalculatorService`、工具类、纯业务计算逻辑

这类测试重点是验证“方法行为”本身，不关心 Spring 注入和 Web 请求链路。建议先从断言、生命周期、参数化测试和异常断言开始练。
参考示例：[`Junit5BasicAnnotationsTest`](src/test/java/org/demo/junit5demo/Junit5BasicAnnotationsTest.java)

### 2) Web MVC 切片测试（只加载 Web 相关组件）

- 定义：使用 `@WebMvcTest` 只验证 Controller、参数绑定、状态码和返回值
- 特点：比集成测试轻量，但比纯单测更接近真实 HTTP 行为
- 适用：`DemoController` 的接口参数校验、响应体断言

这类测试重点是验证接口层契约：请求参数如何绑定、状态码是否正确、返回体是否符合预期；同时通过 `@MockitoBean` 隔离下层 Service 依赖。

> **💡 Tip:** `@WebMvcTest` 仅注入 Web 层核心组件（如 `@Controller`、`@ControllerAdvice`、`@JsonComponent`、`Converter`、`Filter`、`WebMvcConfigurer` 和 `MockMvc`），**不会**加载 `@Service`、`@Component`、`@Repository` 等常规 Bean。

参考示例：[`DemoControllerWebMvcTest`](src/test/java/org/demo/junit5demo/DemoControllerWebMvcTest.java)

### 3) 集成测试（完整 Spring Boot 上下文）

- 定义：使用 `@SpringBootTest` 启动完整应用上下文验证端到端链路
- 特点：覆盖最完整，但启动最慢
- 适用：跨层联调、配置生效验证、真实 Bean 协作验证

这类测试重点是验证“真实上下文”中的协作是否正常，包括配置加载、Bean 装配、请求链路和整体行为。通常数量不需要太多，但要覆盖关键主流程。
参考示例：[`SpringBootIntegrationTest`](src/test/java/org/demo/junit5demo/SpringBootIntegrationTest.java)

## 注解速查表

| 注解/断言 | 简单说明 | 常见使用场景 |
| --- | --- | --- |
| `@Test` | 标记普通测试方法 | 单个功能点验证 |
| `@DisplayName` | 给测试起可读名称 | 报告输出更清晰 |
| `@BeforeEach` / `@AfterEach` | 每个测试前后执行 | 初始化/清理测试数据 |
| `@BeforeAll` / `@AfterAll` | 整个类前后只执行一次 | 初始化/释放全局资源 |
| `@Nested` | 嵌套分组测试 | 按功能分组用例 |
| `@RepeatedTest` | 重复执行同一测试 | 简单稳定性验证 |
| `@ParameterizedTest` | 参数化测试入口 | 用多组输入验证同一逻辑 |
| `@ValueSource` | 提供单参数数据源 | 一组基础值测试 |
| `@CsvSource` | 提供多参数数据源 | 输入-输出对照测试 |
| `@Disabled` | 临时跳过测试 | 标记待修复/暂不执行用例 |
| `@Tag` | 测试打标签 | 按标签筛选执行 |
| `@WebMvcTest` | 只加载 MVC 相关 Bean | Controller 切片测试 |
| `@MockitoBean` | 向 Spring 测试上下文注入 mock Bean | 替换 Controller 依赖服务 |
| `@SpringBootTest` | 启动完整 Spring 上下文 | 集成测试 |
| `@AutoConfigureMockMvc` | 在集成测试里注入 `MockMvc` | 发起 HTTP 级请求断言 |
| `@ActiveProfiles("test")` | 启用测试 profile | 区分测试配置 |
| `@TestPropertySource` | 覆盖测试属性 | 覆盖特定配置项 |
| `assertEquals` | 断言实际值等于期望值 | 结果值校验 |
| `assertTrue` / `assertFalse` | 断言布尔表达式 | 条件判断结果 |
| `assertThrows` | 断言必须抛出指定异常 | 异常分支验证 |
| `assertAll` | 聚合多个断言一起执行 | 同一对象多字段校验 |
| `assertTimeout` | 断言执行时间不超时 | 性能基线约束 |
