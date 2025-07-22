# 基于Jwt实现的鉴权登录
## 基本流程
1. 发送请求到服务端, 
   + 如果没有携带token, 重定向到鉴权页面
   + 如果携带token, 尝试解析token, 解析成功放行, 解析失败重定向到鉴权页面
2. 在鉴权页面成功通过认证, 服务端生成Jwt(字符串)返回给客户端
3. 客户端存储服务端下发的Jwt, 存储在Cookie或者其他位置

## Maven依赖

依赖说明或使用说明查看[jjwt](https://github.com/jwtk/jjwt)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- or jjwt-gson if Gson is preferred -->
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```