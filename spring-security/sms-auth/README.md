# 短信认证 Demo

通过手机号 + 短信验证码完成认证，无需密码。适用于移动应用和无密码登录场景。

## 认证流程

### 高层架构流程

![短信认证宏观架构流程](../assets/sms-login-macro-flow.svg)

上图展示了涉及用户、应用系统、短信服务商、数据库和 Redis 五个主要角色的完整认证流程。关键步骤包括：

1. **验证码申请**：用户向应用系统请求验证码
2. **短信发送**：应用系统调用短信服务商 API 发送验证码
3. **缓存存储**：应用系统将验证码缓存到 Redis（设置过期时间）
4. **验证提交**：用户向应用系统提交手机号和验证码
5. **验证比对**：应用系统从 Redis 读取缓存验证码进行比对
6. **用户查询**：验证通过后，应用系统从数据库查询用户信息
7. **Token 签发**：应用系统签发 JWT Token 返回给用户

### 系统内部流程详解

![短信验证码登录流程](../assets/sms-login-flow.svg)

在系统内部，短信认证的具体实现流程如下：

**验证码申请阶段：**

1. **发送验证码请求**：用户调用 `/send-code` 接口，提供手机号
2. **生成验证码**：系统生成指定长度的随机验证码
3. **调用短信服务**：系统调用短信服务商 API，将验证码发送到用户手机
4. **缓存验证码**：验证码存入 Redis，设置过期时间（默认 5 分钟）

**登录认证阶段：**

5. **提交认证请求**：用户收到短信后，向 `/sms-login` 提交手机号 + 验证码
6. **过滤器拦截**：`SmsAuthenticationFilter` 拦截请求，从参数中提取手机号和验证码，构建 `SmsAuthenticationToken`（未认证状态）
7. **认证处理**：`AuthenticationManager` 将未认证的 Token 委托给 `SmsAuthenticationProvider` 处理
8. **验证码校验**：Provider 从 Redis 获取缓存的验证码，与提交的验证码进行比对
9. **用户加载**：验证码匹配后，从数据库加载用户信息，返回已认证的 Token
10. **签发 JWT**：`AuthenticationSuccessHandler` 基于用户信息签发 JWT Token 返回给客户端

**后续请求阶段：**

11. **携带 Token**：客户端在 HTTP 请求头中携带 JWT Token
12. **验证 Token**：`JwtFilter` 拦截请求，验证 Token 有效性，建立用户会话

## 项目结构

```
sms-auth/
├── src/main/java/com/demo/
│   ├── SmsAuthApplication.java         # 启动类
│   ├── config/
│   │   └── SecurityConfig.java         # Spring Security 配置
│   ├── filter/
│   │   ├── JwtFilter.java              # JWT 验证过滤器
│   │   └── SmsAuthenticationFilter.java # 短信认证过滤器（拦截 /sms-login）
│   ├── provider/
│   │   └── SmsAuthenticationProvider.java # 验证码校验和用户加载
│   ├── token/
│   │   └── SmsAuthenticationToken.java  # 自定义认证 Token（封装手机号和验证码）
│   └── controller/
│       └── HelloController.java        # 测试控制器
├── src/main/resources/
│   └── application.yml                 # 应用配置
└── pom.xml
```

## 快速开始

### 1. 配置 Spring Security 支持短信认证

短信认证需要自定义 Spring Security 的三个核心组件，详见源代码：

**自定义 AuthenticationToken**：[`SmsAuthenticationToken`](src/main/java/com/demo/token/SmsAuthenticationToken.java)
- 封装手机号作为 Principal
- 在未认证状态下，凭证为验证码；在已认证状态下，存储用户信息

**自定义 AuthenticationProvider**：[`SmsAuthenticationProvider`](src/main/java/com/demo/provider/SmsAuthenticationProvider.java)
- 实现验证码校验逻辑（从 Redis 获取缓存验证码进行比对）
- 校验通过后从数据库加载用户信息，返回已认证 Token

**自定义 AuthenticationFilter**：[`SmsAuthenticationFilter`](src/main/java/com/demo/filter/SmsAuthenticationFilter.java)
- 拦截 POST `/sms-login` 请求
- 从请求参数提取手机号和验证码，构建未认证 Token
- 委托 `AuthenticationManager` 进行认证

**在 SecurityFilterChain 配置中**：[`SecurityConfig`](src/main/java/com/demo/config/SecurityConfig.java)
- 注册自定义 Provider 到 `AuthenticationManager`
- 将自定义 Filter 添加到过滤链
- 配置 `/sms-login`、`/send-code` 为公开端点
- 其他资源需要认证

> [!TIP]
> 短信认证使用自定义 Token 和 Provider，因此需要完整的三层配置，比表单认证复杂。

### 2. 配置 application.yml

编辑 `src/main/resources/application.yml`，配置以下参数：

```yaml
# JWT 相关配置
jwt:
  secret: your-secret-key-here           # JWT 密钥
  expiration: 3600000                     # Token 过期时间（毫秒）

# 短信验证码相关配置
sms:
  code:
    length: 6                             # 验证码长度
    expiration: 300                       # 验证码过期时间（秒），300 = 5分钟
    maxAttempts: 5                        # 最大验证次数

# Redis 相关配置
spring:
  redis:
    host: localhost                       # Redis 服务器地址
    port: 6379                            # Redis 端口
    password:                             # Redis 密码（无密码时留空）
    timeout: 2000                         # 连接超时时间（毫秒）

# 数据库相关配置
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/demo  # MariaDB 连接 URL
    username: root                        # 数据库用户名
    password: your-db-password            # 数据库密码
```

### 3. 启动项目

```bash
# 编译并启动
mvn clean spring-boot:run

# 或使用 IDE 直接运行 SmsAuthApplication.main()
```

应用启动后访问：http://localhost:8080

### 4. 发送短信验证码

```bash
# 请求验证码（演示版本会在控制台输出验证码）
curl -X POST http://localhost:8080/send-code \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "phone=13800138000"

# 响应示例：
# {
#   "code": 200,
#   "msg": "验证码已发送到手机"
# }

# 检查控制台输出，获取验证码（如：123456）
```

> [!TIP]
> 演示版本在控制台输出验证码便于测试。生产环境需集成真实短信服务商 API（如阿里云、腾讯云）。

### 5. 短信登录

```bash
# 提交手机号 + 验证码进行登录
curl -X POST http://localhost:8080/sms-login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "phone=13800138000&code=123456"

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
> 确保数据库中存在该手机号对应的用户。

### 6. 使用 Token 访问受保护资源

```bash
# 在请求头中携带 Token
curl http://localhost:8080/api/hello \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
```

## 接口文档

### 公开接口

| 路径 | 方法 | 说明 | 参数 |
|------|------|------|------|
| `/` | GET | 主页 | - |
| `/send-code` | POST | 发送短信验证码 | `phone`: 手机号 |
| `/sms-login` | POST | 短信登录 | `phone`: 手机号, `code`: 验证码 |

### 受保护接口

| 路径 | 方法 | 说明 | 所需权限 |
|------|------|------|----------|
| `/api/hello` | GET | 获取问候信息 | 已认证（ROLE_USER） |
| `/api/admin` | GET | 管理员资源 | 已认证（ROLE_ADMIN） |
