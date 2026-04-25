# Jwt

## Jwt结构

JWT 由Header、Payload、Signature三部分组成， 基本结构如下：

```bash
# Jwt结构
header.payload.signature

# 演示Jwt
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30
```

### Header

Header常见内容如下： 

```json
// json
{
  "alg": "HS256",   // 签名算法（如 HMAC SHA256）
  "typ": "JWT"      // Token 类型
}
```

在生成Jwt的过程中会将这个部分的内容(Json)进行Base64Url编码

### Playload

Playload常见内容：

```json
// 这些属性并不是强制的， 仅仅作为标准规范参考
{
  "iss": "auth.example.com",      // 签发者（Issuer）：谁签发了这个 JWT，一般是认证服务器的标识
  "sub": "1234567890",            // 主题（Subject）：这个 JWT 针对的用户或实体的唯一标识
  "aud": "my-app",                // 受众（Audience）：这个 JWT 的接收方，通常是你的应用系统标识
  "exp": 1718000000,              // 过期时间（Expiration Time）：JWT 的失效时间（Unix 时间戳，单位秒）
  "nbf": 1717990000,              // 生效时间（Not Before）：JWT 在此时间之前无效（Unix 时间戳，单位秒）
  "iat": 1717990000,              // 签发时间（Issued At）：JWT 的签发时间（Unix 时间戳，单位秒）
  "jti": "abc123"                 // JWT ID（JWT ID）：JWT 的唯一标识，用于防止重放攻击
}
```

在生成Jwt的过程中会将这个部分的内容(Json)进行Base64Url编码

### Signature

这个部分并不存储实际的内容，生成过程如下：

1. 拼接 Header 和 Payload 的编码结果：

```json
  concatenateStr =  base64UrlEncode(header) + "." + base64UrlEncode(payload)
```

2. 利用秘钥(字符串)和指定的算法进行签名：
```json
   signature = HMACSHA256(concatenateStr, secret)
```

3. 进行Base64Url编码


> [!WARNING]
>  1. Playload和Header是明文，Signature是密文
>  2. Jwt不具备保密性（不要将敏感信息存储到Payload），仅能够保证信息没有被篡改和发送者身份真实性


## 工作流程

Jwt的工作流程包含两个部分：

1. Token的颁发：

   ![JWT Token颁发流程](./assets/jwt-issue-flow.svg)

2. 受保护资源的访问:

   ![JWT 受保护资源访问流程](./assets/jwt-access-flow.svg)


## 扩展JWT方案

### 双Token方案

颁发Token时颁发两个Token： RfreshToken和AccessToken, 其中AccessToken的过期时间较短，RefreshToken的过期时间较长. AccessToken用于访问受保护资源， RefreshToken用于刷新AccessToken.

![Jwt 双Token方案](./assets/jwt-double-token-flow.svg)

## SpringBoot集成Jwt

### Maven依赖

推荐使用统一版本变量，避免 `jjwt-api / jjwt-impl / jjwt-jackson` 版本不一致。

依赖说明与更多用法可查看 [jjwt 官方仓库](https://github.com/jwtk/jjwt)。

```xml
<properties>
    <jjwt.version>0.12.6</jjwt.version>
</properties>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- 如果项目用 Gson，可替换为 jjwt-gson -->
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
```

依赖作用说明：

- `jjwt-api`：对外 API（编译期需要）。
- `jjwt-impl`：核心实现（运行期需要）。
- `jjwt-jackson`：基于 Jackson 的 JSON 序列化支持（运行期需要）。

## Jwt封装工具类

本项目已封装 JWT 相关功能，提供统一、易用的接口，便于在 Spring Boot 项目中直接集成和使用。

**相关类:**

- [TokenManager](./src/main/java/org/demo/manager/jwt/TokenManager.java)  
  主要职责：JWT 的生成、校验、解析的统一入口。  
  主要方法：`generateToken`、`validateToken`、`parseToken`

- [TokenProperties](./src/main/java/org/demo/manager/jwt/TokenProperties.java)  
  主要职责：JWT 相关配置的载体，自动绑定 application.yml 配置。参考application.yml配置如下：
    ```yaml
    # application.yml
    jwt:
    token:
        secret: your-base64-secret # JWT 签名密钥，建议使用 base64 编码字符串
        expiration-seconds: 86400 # Token有效期，单位秒（如 24 小时）
    ```

- [JwtUtil](./src/main/java/org/demo/manager/jwt/JwtUtil.java)  
  主要职责：底层 JWT 生成与解析的工具方法，供 TokenManager 调用。
