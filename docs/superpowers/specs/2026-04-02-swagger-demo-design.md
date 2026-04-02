标题: Swagger Demo 设计（结构化示例）
日期: 2026-04-02
模块: swagger3

概述
----
本设计用于在现有 swagger3 模块上实现一个结构化的 springdoc-openapi (Swagger3) 演示，覆盖基础注解、接口分组、安全认证、统一返回、常用配置以及进阶功能（文件上传、枚举、响应码等）。目标是提供教学级别、易扩展的示例代码与配置，方便在本仓库中运行、演示与迁移到实际项目。

设计决策
----
- 采用「结构化 Demo」方案：按功能拆分 Controller（basic、public、admin、security、advanced），每个 Controller 聚焦一组主题。理由：清晰、便于演示分组与安全切换，也便于扩展与维护。
- 优先使用现有 pom 中 springdoc-openapi-starter-webmvc-ui 依赖（版本 2.8.9），与模块当前 Spring Boot 版本保持一致。

交付物（预期新增/修改文件）
----
- src/main/resources/application.yml
- src/main/java/com/example/swagger3/config/OpenApiConfig.java
- src/main/java/com/example/swagger3/controller/basic/BasicController.java
- src/main/java/com/example/swagger3/controller/public/PublicController.java
- src/main/java/com/example/swagger3/controller/admin/AdminController.java
- src/main/java/com/example/swagger3/controller/security/SecurityController.java
- src/main/java/com/example/swagger3/controller/advanced/AdvancedController.java
- src/main/java/com/example/swagger3/dto/UserDto.java
- src/main/java/com/example/swagger3/dto/Result.java
- src/main/java/com/example/swagger3/enum/RoleEnum.java
- src/test/java/com/example/swagger3/OpenApiSmokeTest.java (可选)

说明: 文档中原使用的 <base> 占位符已替换为示例模块根包 com.example.swagger3。实现时请将此包替换为你项目实际的 Java 包根路径（特别是在多模块仓库中）。

依赖与兼容性（Dependencies & Compatibility）
----
- springdoc-openapi-starter-webmvc-ui: 2.8.9（已在 pom.xml 中声明）
- 本仓库父 POM 指定的 Spring Boot 版本：3.5.3（见仓库根 pom.xml）。已在本模块执行依赖树检查，springdoc-openapi-starter-webmvc-ui:2.8.9 与 Spring Boot 3.5.x 在当前代码库中工作。如在未来升级 Spring Boot，请参考 https://springdoc.org/compatibility.html 并根据兼容表调整 springdoc 版本。
- 实施步骤：在实现前可运行 `mvn -pl swagger3 -DskipTests dependency:tree` 来确认本模块的 Spring Boot 相关依赖；我已为你执行过一次检查并确认当前兼容性（见实现记录）。

实现要点（按清单项映射）
----
1) 项目搭建
  - 新增 application.yml，包含 OpenAPI 元信息、Swagger UI 路径自定义（可选）、api-docs 路径自定义。
  - README 补充启动与访问说明。

2) 基础注解使用
  - BasicController 演示 @Tag、@Operation、@Parameter、@Hidden 等注解用法。
  - DTO 使用 @Schema 注解（类与字段），字段包含 example 与 description。

3) 接口分组
  - 在 OpenApiConfig 中创建 GroupedOpenApi Bean（public-api、admin-api），并通过 packagesToScan 或 pathsToMatch 将 Controller 归属不同分组。

4) 安全认证
  - 在 OpenApiConfig 中定义 SecuritySchemes（basicAuth、bearerAuth），并在需要的 Operation 或 Controller 上使用 @SecurityRequirement。
  - Swagger UI 将展示 Authorize 按钮，能输入 Basic / Bearer Token。

安全演示细节（Security demo details）
  - 建议在 demo 中使用一个简单的“mock”验证实现或最小可运行的 in-memory Spring Security 配置：推荐使用 in-memory 用户（用户名 admin 密码 admin）和一个静态 demo JWT（例如: "Bearer demo-admin-token"）作为示例。这样既能在 UI 中演示授权流程，又不会引入真实密钥管理问题。
  - 建议将安全示例映射目的：
    - /admin/** - 受保护（示例：需要 Bearer token 或 Basic，演示带有角色 ADMIN 的保护）；
    - /security/** - 用于展示 @SecurityRequirement 注解（无需复杂验证逻辑，只需展示 UI 行为）。
  - 文档中将列出示例凭据/Token：
    - Basic: username=admin password=admin
    - Bearer: Bearer demo-admin-token
  - 注意 CORS/CSRF：若使用 Spring Security 的表单/CSRF，需在 demo 中为 Swagger UI 的 Try-it-out 操作允许跨域或临时禁用 CSRF，以便 UI 能直接发起 POST/PUT 请求（在生产环境中不要禁用 CSRF）。
  - 警告：不要在仓库中提交真实凭据或私钥；所有示例仅用于本地演示。

最小 Security 配置示例（便于 Try-it-out 在本地非生产环境工作）
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .csrf().disable() // demo 仅用于本地演示，生产环境请启用并正确配置 CSRF
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/admin/**").hasRole("ADMIN")
              .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
              .anyRequest().permitAll()
          )
          .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
```

在 OpenApiConfig 中注册 SecuritySchemes 的最小示例：
```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes("basicAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))
            .addSecuritySchemes("bearerAuth",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
        )
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
}
```

5) 统一返回结果
  - 定义泛型 Result<T>，使用 @Schema 注解描述 code/message/data；在 Controller 的 @ApiResponse/@Operation 中声明返回 schema，确保 data 的实际类型能在文档中显示。

OpenAPI 与泛型包装类型（Generics and Result<T>）
  - 说明：OpenAPI 对泛型包装类型的展示不总是自动推断出内部 data 的具体 schema。实现时，请在每个需要的 Controller 方法上显式声明返回 content 的实现类型，例如：
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserDto.class)))
  - 或者使用 @Operation(responses = { @ApiResponse(content = @Content(schema = @Schema(oneOf = { Result.class, UserDto.class }))) }) 的类似写法来确保 UI 显示 data 的具体结构。
  - 本规范建议在 BasicController 中至少使用一处示例注解，作为模板供其他方法复用。

示例（注解展示 Result<UserDto>）：
```java
// Controller 方法示例，显示如何在 OpenAPI 中让 data 的类型为 UserDto
@Operation(summary = "Get user by id")
@ApiResponse(responseCode = "200", description = "OK",
    content = @Content(mediaType = "application/json",
        schema = @Schema(implementation = ResultUserDtoWrapper.class)))
public Result<UserDto> getUser(@PathVariable Long id) {
    // ... implementation
}

// 辅助类用于告诉 OpenAPI the wrapper schema should contain UserDto in data
@Schema(name = "ResultUserDtoWrapper")
public static class ResultUserDtoWrapper extends Result<UserDto> {}
```

说明：这里使用一个具体的继承类型（ResultUserDtoWrapper）或在 @ApiResponse 中直接指定 @Schema(implementation = UserDto.class) 的方式，能确保 swagger 显示 Result.data 的具体 schema（不同的生成器对泛型的推断不同，使用显式声明是最可靠的方法）。

6) 常用配置
  - application.yml 或 OpenApiConfig 中配置自定义访问路径、接口排序、默认展开/折叠、扫描包路径与过滤规则。

7) 进阶功能
  - 自定义响应状态码描述（@ApiResponses/@ApiResponse）。
  - 文件上传接口示例（multipart/form-data 标注）。
  - 枚举类型参数示例（枚举类带 @Schema，使用处引用）。
  - 接口排序控制示例（OpenApi customizer 或 Swagger UI 配置）。

接口排序注意事项
  - 说明：Controller 上使用 @Order 并不会影响最终在 Swagger UI 中的接口排序。若需要控制顺序，请使用 OpenApiCustomiser 在生成阶段对 operations 列表排序，或通过 Swagger UI 的配置项 operationsSorter / tagsSorter 来控制前端显示顺序。

验收标准（Acceptance Criteria）
----
以下验收项应尽可能通过自动化测试或明确的手动验证步骤完成：

1) 基本可达性
  - Swagger UI 在浏览器可访问（默认或自定义 path）；示例 URL: http://localhost:8080/swagger-ui/index.html 或配置的 /docs/swagger-ui.html
  - /v3/api-docs 返回 200 并为 JSON 格式。

2) 文档结构
  - /v3/api-docs 的 JSON 包含预期 tags: ["basic","public","admin","security","advanced"]。
  - components.schemas 中包含 UserDto 与 Result 的 schema。
  - paths 中包含至少一个示例 path：/basic/ping 或 /basic/hello。

3) 安全演示
  - Swagger UI 的 Authorize 支持输入 Basic 和 Bearer。
  - 使用示例凭据（Basic: admin/admin；Bearer: Bearer demo-admin-token）执行 Try-it-out 能成功调用受保护的 demo endpoint（手动或自动化验证）。

4) 统一返回类型
  - 在文档中，返回体显示为 Result 且 data 的实际类型（例如 UserDto）可见（通过 @ApiResponse 显式声明或 schema 实现）。

5) 文件上传
  - 在 Swagger UI 中，文件上传端点展示 multipart/form-data 的文件选择控件。
  - 交付至少一种验证方式：
    A) 自动化：提供 MockMvc 的 multipart 测试，向 /files/upload 发起带文件的 POST 并断言 200。
  B) 手动：在 README 提供 curl 示例：
     curl -v -F "file=@./sample.txt" http://localhost:8080/files/upload

示例 MockMvc multipart 测试片段：
```java
@SpringBootTest
@AutoConfigureMockMvc
public class FileUploadTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void uploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt",
            "text/plain", "hello".getBytes());

        mvc.perform(MockMvcRequestBuilders.multipart("/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
```

建议在 test/resources 中放置一个小样例文件，或使用 MockMultipartFile 如上创建。

6) 枚举展示
  - 枚举类型在 schema 中显示其可选值（在 enum 字段或 allowableValues 中），并在 DTO 字段上标明序列化类型（string/number）。

7) 接口排序与 UI 配置
  - 如在 spec 中配置操作排序或 Swagger UI sorter，UI 展示顺序应与配置一致（手动验证）。

8) 安全与风险管控
  - README 中包含运行时说明，明确指出这些示例仅用于本地或演示环境，不应在生产环境中直接暴露 Swagger UI / demo credentials。

时间线估算
----
- 总计约 5 天（单人开发，含验证与 README）：
  - 准备配置与 README：0.5 天
  - 基础注解与实体：1 天
  - 分组：0.5 天
  - 安全认证：1 天
  - 统一返回：0.5 天
  - 常用配置与进阶功能：1.5 天
  - 测试与文档：0.5 天

实现顺序（推荐）
----
1. 新增 application.yml 与 README 补充（在 README 中加入运行、curl 示例和演示凭据）
2. 实现 DTO（Result、UserDto）与 BasicController（包含示例 @ApiResponse 注解来演示泛型包装）
3. 添加 OpenApiConfig（OpenAPI 元信息、GroupedOpenApi）并配置 packagesToScan 或 pathsToMatch
4. 将 Controller 放入分组包，验证分组
5. 添加 security schemes 与 SecurityController（实现简单 in-memory 或静态 token 验证），验证 Authorize
6. 添加 advanced 示例（文件上传、枚举、状态码）并提供自动化或手动验证步骤
7. 测试、README 完善与风险说明

提交策略（分支与提交粒度）
----
- 建议新分支： feature/swagger-demo-structured
- 提交粒度示例：
  1) add application.yml and README
  2) add DTO Result and UserDto
  3) add BasicController
  4) add OpenApiConfig with groups
  5) add security schemes and SecurityController
  6) add advanced features
  7) add tests and polish README

后续步骤
----
1. 我已把设计写入本文件（docs/superpowers/specs/2026-04-02-swagger-demo-design.md）。
2. 建议下一步：我将再次调度规格审查子代理（spec-document-reviewer）对本更新后的设计做一次复审，确认高优先级改动是否充分覆盖。如你同意，我现在开始调度审查并在收到结果后把审查报告贴给你。

3. 注意：在开始代码实现前，请确认以下开放问题（供最终实现决策）：
  - 是否要在 demo 中提供一个本地的 JWT 发行端点（/auth/token）来动态生成 demo token？（推荐：可选，增加 ~0.5 天）
  - 是否要求将 Swagger UI 仅在非生产 profile 下启用？（推荐：是）

作者: OpenCode

作者: OpenCode
