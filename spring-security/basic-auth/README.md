# Basic 认证 Demo

基于 HTTP Authorization 请求头的简单认证方式，凭证为 `base64(username:password)`。适合内部服务、API 调试或简单场景。

## 认证流程

![HTTP Basic 认证流程](../assets/basic-auth-flow.svg)

### 工作原理

1. **首次请求**：客户端访问受保护资源，未提供凭证
2. **服务端返回 401**：服务端返回 `401 Unauthorized` 响应，包含 `WWW-Authenticate: Basic realm="..."`
3. **浏览器弹框**：浏览器弹出登录对话框，要求输入用户名和密码
4. **客户端编码**：浏览器将 `username:password` 进行 Base64 编码
5. **发送凭证**：浏览器在 `Authorization` 请求头中发送 `Basic <base64_string>`
6. **服务端验证**：服务端解码凭证并验证
7. **验证通过**：返回受保护的资源

特点：无状态（无需 Session），每次请求都需提供凭证。

## 项目结构

```
basic-auth/
├── src/main/java/com/demo/
│   ├── BasicAuthApplication.java       # 启动类
│   ├── config/
│   │   └── SecurityConfig.java         # Spring Security 配置
│   └── controller/
│       └── HelloController.java        # 测试控制器
├── src/main/resources/
│   └── application.yml                 # 应用配置
└── pom.xml
```

## 快速开始

### 1. 配置 Spring Security 支持 HTTP Basic 认证

在 [`SecurityConfig`](src/main/java/com/demo/config/SecurityConfig.java) 中进行以下配置：

- **启用 HTTP Basic 认证**：使用 `httpBasic(Customizer.withDefaults())` 启用
- **配置 UserDetailsService**：见 [`SecurityConfig`](src/main/java/com/demo/config/SecurityConfig.java) 中的 `userDetailsService()` 方法，配置用户信息查询逻辑（查询出来的用户信息用于与提交的信息比较）

> [!TIP]
> HTTP Basic 认证由 Spring Security 内置支持，无需额外的自定义 Filter 或 Provider。

### 2. 启动项目

```bash
# 编译并启动
mvn clean spring-boot:run

# 或使用 IDE 直接运行 BasicAuthApplication.main()
```

应用启动后访问：http://localhost:8080

### 3. 访问受保护资源（使用 curl）

**方式 1：使用 `-u` 参数（推荐）**

curl 会自动进行 Base64 编码和设置请求头：

```bash
# 格式：curl -u username:password <url>
curl -u admin:123456 http://localhost:8080/api/hello

# 响应示例：
# Hello admin, you have ROLE_ADMIN!
```

**方式 2：手动设置 Authorization 请求头**

```bash
# 手动 Base64 编码
curl -H "Authorization: Basic $(echo -n 'admin:123456' | base64)" \
  http://localhost:8080/api/hello
```

**方式 3：浏览器访问**

直接访问 http://localhost:8080/api/hello，浏览器会弹出登录对话框，输入用户名和密码即可。

> [!WARNING]
> 浏览器会缓存 HTTP Basic 凭证。要切换用户，可用无痕模式或清除浏览器缓存。

### 4. 测试不同用户和权限

```bash
# 使用 admin 用户（具有 ADMIN 角色）
curl -u admin:123456 http://localhost:8080/api/admin

# 使用 user 用户（仅 USER 角色）
curl -u user:123456 http://localhost:8080/api/admin
# 返回 403 Forbidden
```

## 接口文档

### 公开接口

| 路径 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 主页 |
| `/hello` | GET | 问候页面 |

### 受保护接口

| 路径 | 方法 | 说明 | 所需角色 |
|------|------|------|----------|
| `/api/hello` | GET | 获取问候信息 | ROLE_USER |
| `/api/admin` | GET | 管理员资源 | ROLE_ADMIN |
| `/api/user-info` | GET | 获取当前用户信息 | ROLE_USER |
