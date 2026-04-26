# Swagger3

本模块用于集成和演示如何在 Spring Boot 项目中使用 springdoc-openapi（Swagger3/OpenAPI 3）进行接口文档自动生成。

## 项目结构

```
swagger3/
├── src/main/java/com/example/swagger3/
│   ├── Swagger3Application.java          # 启动类
│   ├── config/
│   │   ├── OpenApiConfig.java            # OpenAPI 配置（安全、分组）
│   │   └── SecurityConfig.java           # Spring Security 配置
│   ├── controller/
│   │   ├── basic/BasicController.java    # 基础 API
│   │   ├── admin/AdminController.java    # 管理 API
│   │   ├── security/SecurityController.java  # 安全认证示例
│   │   ├── pub/PublicController.java     # 公开 API
│   │   └── advanced/AdvancedController.java  # 高级功能
│   ├── dto/
│   │   ├── Result.java                   # 统一响应包装
│   │   └── UserDto.java                  # 用户 DTO
│   └── enums/RoleEnum.java               # 角色枚举
├── src/main/resources/
│   └── application.yml                   # 应用配置
└── pom.xml                               # Maven 配置
```

## 快速开始

### 添加依赖

在 `pom.xml` 中添加如下依赖：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.9</version>
</dependency>
```

### 启动应用

```bash
mvn spring-boot:run -DskipTests
```

应用默认运行在 `http://localhost:8080`

### 访问接口文档和测试界面

启动应用后，打开浏览器访问：

- **Swagger UI 测试界面**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON 规范**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> [!NOTE]
> springdoc-openapi 支持开箱即用，通常无需额外配置。只需保证你的 Controller 使用了标准的 Spring Web 注解（如 `@RestController`、`@RequestMapping` 等）。

### 在 Swagger UI 中测试

#### 不需认证的接口

直接在 Swagger UI 中点击相应接口，点击 **Try it out** 按钮进行测试。

#### Bearer Token 认证

1. 点击右上角的 **Authorize** 按钮
2. 在弹出框中选择 **bearerAuth**
3. 输入 Bearer Token: `demo-admin-token`（这是 demo 中配置的演示 token）
4. 点击 **Authorize** 按钮

之后所有需要认证的接口都会自动在请求头中添加此 Token。

## 配置安全认证方案

### Bearer Token 方案

在 `OpenApiConfig.java` 中定义 Bearer Token 认证方案：

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .components(new Components()
                    .addSecuritySchemes("bearerAuth", 
                            new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
}
```

**代码说明：**

| 配置项 | 说明 |
|--------|------|
| `.addSecuritySchemes("bearerAuth", ...)` | 定义一个名为 `bearerAuth` 的认证方案 |
| `.type(SecurityScheme.Type.HTTP)` | 使用 HTTP 认证 |
| `.scheme("bearer")` | Bearer Token 认证方式 |
| `.bearerFormat("JWT")` | 标记为 JWT 格式（用于文档说明） |
| `.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))` | 设置全局默认安全要求 |

**作用：**
- 在 Swagger UI 的 **Authorize** 按钮中显示 Bearer Token 认证选项

**在接口中使用认证**

在 Controller 中通过 `@SecurityRequirement` 注解指定认证方式：

```java
@GetMapping("/secured")
@Operation(summary = "受保护接口示例")
@SecurityRequirement(name = "bearerAuth")  // 指定使用 bearerAuth
public Result<String> secured() {
    return new Result<>(200, "ok", "secured data");
}
```

**移除接口的认证要求**

如果某个接口不需要认证（如公开接口），可以显式移除：

```java
@GetMapping("/public")
@Operation(summary = "公开接口")
@SecurityRequirement(name = "")  // 空字符串表示不需要认证
public Result<String> publicEndpoint() {
    return new Result<>(200, "ok", "data");
}
```

## 配置 API 分组

### API 分组的作用

API 分组可以：
- 将相关功能的接口集中在一起，使文档更清晰
- 在 Swagger UI 中显示为不同的选项卡
- 便于大型项目的接口管理和维护

### 当前的 API 分组

demo 中定义了 5 个分组，在 `OpenApiConfig.java` 中：

```java
@Bean
public GroupedOpenApi basicApi() {
    return GroupedOpenApi.builder()
            .group("basic")                                      // 分组名称
            .packagesToScan("com.example.swagger3.controller.basic")  // 扫描的包路径
            .build();
}

@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
            .group("public")
            .packagesToScan("com.example.swagger3.controller.pub")
            .build();
}

// ... 类似的 securityApi, adminApi, advancedApi
```

**分组说明：**

| 分组名 | 包路径 | 功能 |
|-------|---------|------|
| basic | `controller.basic` | 基础功能演示 |
| public | `controller.pub` | 公开接口（无需认证） |
| security | `controller.security` | 安全认证演示 |
| admin | `controller.admin` | 管理员接口 |
| advanced | `controller.advanced` | 高级功能（文件上传、枚举等） |

### 添加新的 API 分组

如果想为新的功能模块添加 API 分组，按以下步骤操作：

**步骤 1：** 创建新的 Controller 包和类

```
src/main/java/com/example/swagger3/controller/order/
├── OrderController.java
└── OrderService.java
```

**步骤 2：** 在 `OpenApiConfig.java` 中添加新的分组 Bean

```java
@Bean
public GroupedOpenApi orderApi() {
    return GroupedOpenApi.builder()
            .group("order")
            .packagesToScan("com.example.swagger3.controller.order")
            .build();
}
```

**步骤 3：** 在 OrderController 中添加 API

```java
@RestController
@RequestMapping("/order")
@Tag(name = "order", description = "订单管理接口")
public class OrderController {
    
    @PostMapping
    @Operation(summary = "创建订单")
    @SecurityRequirement(name = "bearerAuth")
    public Result<OrderDto> createOrder(@RequestBody OrderDto order) {
        return new Result<>(200, "ok", order);
    }
}
```

**步骤 4：** 重启应用后，Swagger UI 中就会出现新的 "order" 分组

## 导出到测试工具

### 获取 OpenAPI JSON

#### 浏览器下载

1. 访问 http://localhost:8080/v3/api-docs
2. 将整个 JSON 内容保存为 `openapi.json` 文件

#### 命令行下载

```bash
curl -o openapi.json http://localhost:8080/v3/api-docs
```

### 导入到ApiFox

1. 打开ApiFox
2. 点击导入项目
3. 选择OpenApi/Swagger
4. 选择文件导入或URL导入（这里以URL导入为例）
5. 输入 `http://localhost:8080/v3/api-docs`
6. 按照提示修改信息

### 导入到 Postman

1. 打开 Postman
2. 点击 **File** → **Import**
3. 选择 **Link** 标签页
4. 输入 `http://localhost:8080/v3/api-docs`
5. 点击 **Continue** 和 **Import**

Postman 会自动创建集合，包含所有 API 端点，并自动配置认证方式。

### 导入到 REST Client（VS Code）

使用 VS Code 的 REST Client 扩展，创建 `.http` 或 `.rest` 文件：

```http
### Ping 测试
GET http://localhost:8080/basic/ping

### 查询用户
GET http://localhost:8080/basic/users/1

### 创建用户
POST http://localhost:8080/basic/users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com"
}

### 获取公开信息
GET http://localhost:8080/public/info

### 文件上传
POST http://localhost:8080/advanced/files/upload
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="test.txt"
Content-Type: text/plain

test file content
------WebKitFormBoundary7MA4YWxkTrZu0gW--

### 枚举参数示例
POST http://localhost:8080/advanced/role?role=USER

### Bearer Token 认证示例
GET http://localhost:8080/security/secured
Authorization: Bearer demo-admin-token

### Admin API（需要Bearer Token）
GET http://localhost:8080/admin/me
Authorization: Bearer demo-admin-token
```

## Swagger注解参考

### 基本注解

用于 API 操作和分组的基础注解：

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Tag` | API 分组和标记 | `@Tag(name = "user", description = "用户管理")` |
| `@Operation` | 操作摘要和详细信息 | `@Operation(summary = "创建用户", description = "...")` |
| `@ApiResponse` | 单个响应状态码和内容描述 | `@ApiResponse(responseCode = "200", description = "成功")` |
| `@ApiResponses` | 多个响应描述 | `@ApiResponses({@ApiResponse(...), @ApiResponse(...)})` |

### 参数注解

用于描述接口参数的注解：

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Parameter` | 路径、查询参数的名称、类型、示例等 | `@Parameter(description = "用户ID", example = "1")` |
| `@RequestBody` | 请求体的内容和格式 | `@RequestBody(description = "用户信息", required = true)` |
| `@RequestParam` | Spring 注解，需配合 @Parameter 提供文档 | `@RequestParam("name") String name` |

### 数据模型注解

用于描述数据模型（DTO、枚举等）的注解：

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Schema` | DTO 和枚举的描述和示例 | `@Schema(description = "用户信息", example = "...")` |
| `@Schema.name` | 自定义 Schema 名称 | `@Schema(name = "User", description = "用户")` |
| `@Schema.example` | 提供示例值 | `@Schema(example = "alice")` |
