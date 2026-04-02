package com.demo.handler;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.demo.entity.TUser;
import com.demo.service.TokenRedisService;
import com.demo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;

/**
 * 认证成功处理器
 * 
 * 职责：
 * 1. 在用户认证成功后被调用
 * 2. 从 Authentication 对象中提取用户信息
 * 3. 生成 JWT Token（包含用户信息）
 * 4. 将 Token 存储到 Redis（TTL: 1 天）
 * 5. 将 Token 返回给客户端
 * 
 * 使用场景：
 * - 表单登录认证成功后
 * 其他认证方式（如 Basic 认证、OAuth2）可能有不同的处理器
 * 
 * 返回格式：
 * {
 *   "code": 200,
 *   "msg": "登录成功",
 *   "data": "<jwt_token>"
 * }
 * 
 * JWT Token 包含信息：
 * - payload.user：用户对象（TUser），包括登录名、用户名等
 * 后续请求中 JwtFilter 会从 Token 中提取用户信息恢复认证状态
 * 
 * Redis 存储：
 * - Key：jwt:token:{tokenHash} — Token 的 SHA256 hash
 * - Value：完整的 JWT Token
 * - TTL：1 天（86400 秒）
 * - 登出时会从 Redis 中删除该 Token
 */
@Slf4j
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${jwt.secret:defaultSecretKey123456}")
    private String secret;

    private final TokenRedisService tokenRedisService;

    public CustomLoginSuccessHandler(TokenRedisService tokenRedisService) {
        this.tokenRedisService = tokenRedisService;
    }

    /**
     * 处理认证成功事件
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应，用于返回 Token
     * @param authentication 认证对象，包含用户信息和权限
     * @throws IOException 写入响应时可能抛出
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 从 Authentication 对象中提取用户信息
        // authentication.getPrincipal() 返回 UserDetails 或自定义对象
        // 本示例为 TUser 对象
        TUser user = (TUser) authentication.getPrincipal();
        log.info("[FormAuth登录成功处理] 用户认证成功: {}", user.getLoginAct());

        // 2. 构建 JWT Token payload
        // payload 是一个 Map，包含需要编码到 Token 中的信息
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("user", user);
        
        // 3. 使用密钥签名生成 JWT Token
        // JWTUtil.createToken() 根据 payload 和 secret 创建 Token
        // Token 包含三部分：Header.Payload.Signature
        // 后续验证时需要相同的 secret 来验证签名有效性
        String token = JWTUtil.createToken(payload, secret.getBytes());
        log.info("[FormAuth登录成功处理] JWT Token已生成");

        // 4. 将 Token 存储到 Redis（TTL: 1 天）
        // 存储的目的：
        // - 登出时可以直接删除 Token
        // - 验证时可以检查 Token 是否已被删除
        // - 实现更精细的 Token 生命周期管理
        tokenRedisService.saveToken(token);
        log.info("[FormAuth登录成功处理] Token已存储到Redis");

        // 5. 构建响应对象
        Result<String> result = Result.success("登录成功", token);
        
        // 6. 设置响应头和内容类型
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        
        // 7. 将响应对象序列化为 JSON 并写入响应流
        response.getWriter().write(JSONUtil.toJsonStr(result));
        log.info("[FormAuth登录成功处理] 登录成功响应已返回客户端");
    }
}

