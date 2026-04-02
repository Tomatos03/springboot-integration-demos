package com.demo.handler;

import cn.hutool.json.JSONUtil;
import com.demo.service.TokenRedisService;
import com.demo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

/**
 * 登出成功处理器
 * 
 * 职责：
 * 1. 在用户登出时被调用
 * 2. 从请求头中获取 Token
 * 3. 从 Redis 中删除该 Token
 * 4. 返回登出成功响应给客户端
 * 
 * 登出流程（JWT + Redis）：
 * 1. 客户端发送 POST /logout 请求，在请求头中携带 Token
 * 2. Spring Security 默认会清理 Session（如果有）
 * 3. 调用本处理器的 onLogoutSuccess() 方法
 * 4. 从 Redis 中删除该 Token
 * 5. 返回成功响应给客户端
 * 6. 后续请求如果再使用该 Token 会被 JwtFilter 拒绝（Token 不存在于 Redis）
 * 
 * Redis 存储方式：
 * - 登入时：Token 存储到 Redis，Key: jwt:token:{tokenHash}，TTL: 1 天
 * - 登出时：删除 Redis 中的 Token
 * - 验证时：检查 Token 是否存在于 Redis
 * 
 * 返回格式：
 * {
 *   "code": 200,
 *   "msg": "登出成功",
 *   "data": null
 * }
 * 
 * 注意：
 * 客户端需要在 Authorization 请求头中携带 Token 以便在登出时删除
 */
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final TokenRedisService tokenRedisService;

    public CustomLogoutSuccessHandler(TokenRedisService tokenRedisService) {
        this.tokenRedisService = tokenRedisService;
    }

    /**
     * 处理登出成功事件
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应，用于返回成功信息
     * @param authentication 认证对象，包含当前用户信息
     * @throws IOException 写入响应时可能抛出
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 1. 从请求头中获取 Token
        // 格式：token: <jwt-token>
        String token = request.getHeader("token");
        
        // 2. 若 Token 存在，从 Redis 中删除
        if (token != null && !token.isEmpty()) {
            tokenRedisService.deleteToken(token);
        }
        
        // 3. 构建成功响应对象
        Result<String> res = Result.success("登出成功");
        
        // 4. 设置响应状态码和内容类型
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        
        // 5. 将响应对象序列化为 JSON 并写入响应流
        response.getWriter().write(JSONUtil.toJsonStr(res));
        
        // 可选：记录日志
        // logger.info("User logged out: {}", authentication.getName());
    }
}
