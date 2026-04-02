# 表单认证 Demo

基于用户名密码登录，服务端签发 JWT Token，存储到 Redis（TTL: 1 天），后续请求携带 Token 完成无状态认证。登出时从 Redis 中删除 Token。

## 认证流程

![表单认证流程](../assets/form-auth-flow.svg)

### 流程详解

1. **用户登录**：用户在前端表单输入用户名和密码，提交至 `/login`
2. **认证验证**：Spring Security 拦截请求，调用 `AuthenticationManager` 进行凭证验证
3. **签发 Token**：认证成功后，`CustomLoginSuccessHandler` 签发 JWT Token 并存储到 Redis（TTL: 1 天），然后返回给客户端
4. **后续请求**：客户端在请求头中携带 Token，`JwtFilter` 验证 Token 签名有效性和 Redis 中是否存在
5. **登出**：调用 `/logout` 时，`CustomLogoutSuccessHandler` 从 Redis 中删除 Token 并返回登出成功响应

## 项目结构

```
form-auth/
├── src/main/java/com/demo/
│   ├── FormAuthApplication.java        # 启动类
│   ├── config/
│   │   └── SecurityConfig.java         # Spring Security 配置
│   ├── filter/
│   │   └── JwtFilter.java              # JWT 验证过滤器
│   ├── handler/
│   │   ├── CustomLoginSuccessHandler.java      # 登录成功处理器（签发 Token 并存储到 Redis）
│   │   ├── CustomLoginFailureHandler.java      # 登录失败处理器
│   │   └── CustomLogoutSuccessHandler.java     # 登出处理器（从 Redis 删除 Token）
│   ├── service/
│   │   └── TokenRedisService.java      # Token Redis 存储服务
│   └── controller/
│       └── HelloController.java        # 测试控制器
├── src/main/resources/
│   └── application.yml                 # 应用配置
└── pom.xml
```

## 快速开始

### 1. 配置 Spring Security 支持表单认证

在 [`SecurityConfig.java`](src/main/java/com/demo/config/SecurityConfig.java) 中：

- **启用表单登录**：配置登录入口、登录处理地址、登出地址
- **注册事件处理器**：
  - [`CustomLoginSuccessHandler`](src/main/java/com/demo/handler/CustomLoginSuccessHandler.java) — 认证成功时签发 JWT Token 并存储到 Redis
  - [`CustomLoginFailureHandler`](src/main/java/com/demo/handler/CustomLoginFailureHandler.java) — 认证失败时返回错误响应
  - [`CustomLogoutSuccessHandler`](src/main/java/com/demo/handler/CustomLogoutSuccessHandler.java) — 登出时从 Redis 删除 Token
- **注册 JWT 过滤器**：[`JwtFilter`](src/main/java/com/demo/filter/JwtFilter.java) 用于验证后续请求中的 Token 和 Redis 中的存在性
- **注册 Token 存储服务**：[`TokenRedisService`](src/main/java/com/demo/service/TokenRedisService.java) 提供 Token 与 Redis 的交互
- **配置资源权限**：指定哪些资源需要认证、哪些公开

### 2. 配置 application.yml

编辑 `src/main/resources/application.yml`，配置以下参数：

```yaml
server:
  port: 8080

# JWT 相关配置
jwt:
  secret: your-secret-key-here           # JWT 密钥
  expiration: 86400000                    # Token 过期时间（毫秒），86400000 = 1天

# Redis 相关配置（用于存储 Token）
spring:
  data:
    redis:
      host: localhost                     # Redis 服务器地址
      port: 6379                          # Redis 端口
  
  # 数据库相关配置（用于用户查询）
  datasource:
    url: jdbc:mariadb://localhost:3306/demo  # MariaDB 连接 URL
    username: root                        # 数据库用户名
    password: your-db-password            # 数据库密码
```

> [!IMPORTANT]
> 确保 Redis 服务已启动，否则应用启动时会抛出连接异常

### 3. 启动项目

```bash
# 编译并启动
mvn clean spring-boot:run

# 或使用 IDE 直接运行 FormAuthApplication.main()
```

应用启动后访问：http://localhost:8080

### 4. 测试登录

```bash
# 使用表单登录（获取 Token）
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=123456"

# 响应示例：
# {
#   "code": 200,
#   "msg": "登录成功",
#   "data": {
#     "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
#   }
# }
```

> [!WARNING]
> 确保数据库中存在用户，且密码已用 BCrypt 加密存储。

### 5. 使用 Token 访问受保护资源

```bash
# 在请求头中携带 Token
curl http://localhost:8080/api/hello \
  -H "token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
```

> [!TIP]
> Token 在请求头 `token` 字段中传递，`JwtFilter` 会从该请求头中提取、验证签名和检查 Redis 中的存在性。

### 6. 测试登出

```bash
# 登出（从 Redis 删除 Token）
curl -X POST http://localhost:8080/logout \
  -H "token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."

# 响应示例：
# {
#   "code": 200,
#   "msg": "登出成功",
#   "data": null
# }
```

> [!NOTE]
> 登出成功后，该 Token 会从 Redis 中删除，后续请求将被拒绝（403 Forbidden）

## 接口文档

### 公开接口

| 路径 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 主页 |
| `/login` | GET | 登录页面（返回 HTML 表单） |
| `/login` | POST | 登录接口 |

### 受保护接口

| 路径 | 方法 | 说明 | 所需权限 |
|------|------|------|----------|
| `/api/hello` | GET | 获取问候信息 | 已认证（ROLE_USER） |
| `/api/admin` | GET | 管理员资源 | 已认证（ROLE_ADMIN） |
| `/logout` | POST | 登出 | 已认证 |

## 关键配置项

### Redis 配置

Token 存储在 Redis 中，支持以下配置选项：

```yaml
spring:
  data:
    redis:
      host: localhost                     # Redis 服务器地址
      port: 6379                          # Redis 端口
      password:                           # Redis 密码（无密码时留空）
      timeout: 2000ms                     # 连接超时时间
      jedis:
        pool:
          max-active: 8                   # 最大连接数
          max-idle: 8                     # 最大空闲连接数
          min-idle: 0                     # 最小空闲连接数
```

### JWT 配置

```yaml
jwt:
  secret: your-secret-key-here           # JWT 签名密钥（至少 32 位）
  expiration: 86400000                    # Token 过期时间（毫秒），建议与 Redis TTL 保持一致
```

## Token 存储方式

### Redis Key 命名规范

- **格式**：`jwt:token:{tokenHash}`
- **示例**：`jwt:token:abc123def456...` （Token 的 SHA256 hash）
- **TTL**：1 天（86400 秒）

### Token 生命周期

1. **登入时**：
   - 生成 JWT Token（包含用户信息）
   - 计算 Token 的 SHA256 hash
   - 存储到 Redis：`SET jwt:token:{hash} {token} EX 86400`

2. **验证时**：
   - 提取请求头中的 Token
   - 验证 JWT 签名
   - 检查 Token 是否存在于 Redis（防止已删除的 Token）

3. **登出时**：
   - 计算 Token 的 SHA256 hash
   - 从 Redis 删除：`DEL jwt:token:{hash}`
   - 后续请求将被拒绝

## 工作原理

### 登录流程

```
用户 → POST /login → Spring Security Filter Chain
  ↓
AuthenticationManager 验证凭证
  ↓
凭证有效 → CustomLoginSuccessHandler
  ↓
生成 JWT Token
  ↓
存储到 Redis（TTL: 1天）
  ↓
返回 Token 给客户端
```

### 验证流程

```
请求 → JwtFilter 提取 Token
  ↓
验证 JWT 签名（使用 secret）
  ↓
检查 Token 是否存在于 Redis
  ↓
都通过 → 解析用户信息
  ↓
设置到 SecurityContext
  ↓
继续处理业务逻辑
```

### 登出流程

```
用户 → POST /logout (携带 Token)
  ↓
CustomLogoutSuccessHandler
  ↓
从 Redis 删除 Token
  ↓
返回成功响应
  ↓
后续请求被拒绝（Token 不在 Redis 中）
```

## 常见问题

### 1. 登出后仍能使用旧 Token？
确保 JwtFilter 正确检查了 Redis 中的 Token 存在性。参考 `JwtFilter.java` 第 4 步。

### 2. Token 过期时间如何设置？
在 `application.yml` 中配置 `jwt.expiration`（毫秒），建议与 Redis TTL 保持一致（86400000 = 1天）。

### 3. Redis 连接失败怎么办？
检查 Redis 服务是否已启动，确认 `application.yml` 中的 `host` 和 `port` 配置正确。

### 4. Token 不能被多设备共享吗？
当前实现使用 `jwt:token:{tokenHash}` 作为 key，支持多设备登录。每个设备的 Token 都会独立存储。

### 5. 如何实现 Token 刷新（Refresh Token）？
当前实现未包含刷新机制。可扩展 `TokenRedisService` 添加刷新 Token 存储和验证逻辑。
