# jwt-dual-token

演示「双 Token（Access Token + Refresh Token）」方案的最小可运行示例，与兄弟模块 [`Jwt`](../Jwt/) 的单 Token 方案形成对照。

- `accessToken`：有效期短，放在 `Authorization: Bearer` 中访问业务接口。
- `refreshToken`：有效期长，只能调用刷新接口换取新的一对 token。
- 刷新采用「一次性 + 轮换」：每次刷新都会签发新的 refresh token，旧的立即作废。
- 服务端用 `jti` 白名单记录 refresh token 状态，检测到旧 token 重放时吊销整个登录会话。
- 
## 为什么需要双 Token

单 token 方案只有两个选择：有效期长，泄露后风险大；有效期短，用户频繁重新登录。双 Token 把两种诉求拆开：

- access token 短效（默认 15 分钟），即使泄露，攻击窗口也有限。
- refresh token 长效（默认 7 天），但只用于刷新，不能访问业务接口。
- refresh token 在服务端可吊销，用户登出或检测到异常时可以立即失效。

## 工作流程

![双 Token 流程](./assets/dual-token-flow.svg)

流程中的关键节点：

- 受保护资源只校验 access token 的签名、有效期和 `tokenType`。
- access token 失效时返回 401，客户端拿 refresh token 调用刷新接口。
- 刷新接口校验 refresh token 的签名、有效期、`tokenType` 以及服务端会话状态。
- 刷新成功后返回新的 access token 与新的 refresh token，客户端原子替换本地副本。
- refresh token 也失效时，客户端清理本地状态并跳转登录页。

> 流程图中的 refresh 失败统一写作 `refresh_token_invalid_or_expired`。本 demo 拆成
> `refresh_token_invalid`、`refresh_token_expired`、`refresh_token_reused` 三种情况，
> 分别对应业务状态码 `40105`、`40106`、`40107`。

## 项目结构

```text
jwt-dual-token
├── assets/dual-token-flow.svg
└── src/main/java/org/demo/jwt/dual
    ├── config
    │   ├── TokenProperties.java      # jwt.* 配置与启动校验
    │   └── WebConfig.java            # 只拦截 /api/**，放行 /api/token/**
    ├── context
    │   ├── UserContext.java          # ThreadLocal 用户上下文
    │   └── UserInfo.java
    ├── controller
    │   ├── AuthController.java       # 登录、刷新、登出
    │   └── ResourceController.java   # 受保护资源
    ├── dto                           # Result / ResultCode 统一响应与请求/响应 DTO
    ├── exception                     # AuthException + 全局异常处理
    ├── interceptor
    │   └── AccessTokenInterceptor.java
    ├── manager
    │   ├── JwtManager.java           # 签发/校验，强制 tokenType 与 iss
    │   ├── ParsedToken.java
    │   └── TokenType.java
    ├── service
    │   └── AuthService.java          # 登录、轮换刷新、登出
    └── store
        ├── RefreshSession.java
        ├── RefreshTokenStore.java    # 存储抽象，便于替换为 Redis
        └── InMemoryRefreshTokenStore.java
```

## 关键设计

### `tokenType` 声明防止令牌混用

两种 token 用同一个密钥签发，但都写入 `tokenType` 声明：

```json
{
  "iss": "jwt-dual-token-demo",
  "sub": "7",
  "jti": "0e6f...",
  "iat": 1793600000,
  "exp": 1793600900,
  "tokenType": "access",
  "username": "tomato"
}
```

解析时通过 JJWT 的 `require("tokenType", "access")` 强制校验。把 refresh token 放到
`Authorization` 请求头访问业务接口会得到 401，而不是被当成 access token 放行。

### `jti` 与一次性 refresh token

每次签发 refresh token 都会生成唯一的 `jti`，并把 `jti`、`familyId`、用户信息和过期时间写入
`RefreshTokenStore`。刷新时：

- 先验签、验有效期、验 `tokenType`。
- 再按 `jti` 查服务端状态；不存在或已过期则拒绝。
- 用原子操作把当前 `jti` 标记为已使用，再签发新的一对 token。

这样即使 refresh token 本身尚未过期，只要已经被消费过，也无法再次使用。

### 轮换与重放检测

同一次登录产生的所有 refresh token 共享一个 `familyId`。正常情况下每次刷新都产生新
`jti`，旧 `jti` 标记为已使用。如果一个已使用的 `jti` 再次出现，通常意味着 token 泄露：

- 返回 401，业务码 `40107`。
- 删除整个 `familyId` 下的所有会话，攻击者和真实用户都被强制重新登录。


## 配置

```yaml
server:
  port: 8084

jwt:
  issuer: jwt-dual-token-demo
  secret: "${JWT_SECRET:...}"   # Base64 编码，解码后至少 32 字节
  access-expiration-seconds: 900      # 15 分钟
  refresh-expiration-seconds: 604800  # 7 天
```

生成一个新密钥：

```bash
openssl rand -base64 32
```

生产环境请通过环境变量 `JWT_SECRET` 注入，不要提交真实密钥。`TokenProperties` 在启动时会校验
密钥长度以及 refresh 有效期必须大于 access 有效期。

## 运行

在仓库根目录：

```bash
mvn -pl jwt-dual-token spring-boot:run
```

或进入模块目录：

```bash
cd jwt-dual-token
mvn spring-boot:run
```

## 接口与 curl 示例

### 登录并获取双 Token

```bash
curl -s -X POST http://localhost:8084/api/token/login \
  -H 'Content-Type: application/json' \
  -d '{"userId": 7, "username": "tomato"}'
```

请求体可以省略，缺省用户为 `userId=1`、`username=demo`。响应示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "accessExpiresIn": 900,
    "refreshExpiresIn": 604800
  }
}
```

### 访问受保护资源

```bash
curl -i http://localhost:8084/api/protected \
  -H 'Authorization: Bearer <accessToken>'
```

预期响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "message": "Hello, tomato!",
    "userId": 7,
    "username": "tomato"
  }
}
```

### 刷新 Token

```bash
curl -s -X POST http://localhost:8084/api/token/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken": "<refreshToken>"}'
```

响应结构与登录一致，但 `refreshToken` 已经轮换为新值，旧的立即失效。

### 登出

```bash
curl -i -X POST http://localhost:8084/api/token/logout \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken": "<refreshToken>"}'
```

返回 `200 OK`，响应体为 `{"code":0,"message":"已登出","data":null}`，该 `familyId` 下的 refresh token 全部失效。

## 测试

```bash
mvn -pl jwt-dual-token test
```

覆盖内容：

- `JwtManagerTest`：签发/解析、类型混用拒绝、过期拒绝、篡改拒绝、`jti` 唯一性。
- `TokenPropertiesTest`：密钥强度与有效期配置校验。
- `AuthFlowIntegrationTest`：统一响应结构、登录访问、refresh token 越权、轮换、重放吊销、登出、空 token。
