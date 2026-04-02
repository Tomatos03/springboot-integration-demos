package com.demo.handler;

import cn.hutool.json.JSONUtil;
import com.demo.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

/**
 * 认证失败处理器
 * 
 * 职责：
 * 1. 在用户认证失败后被调用
 * 2. 返回错误响应给客户端
 * 3. 通常用于日志记录、统计等
 * 
 * 失败场景：
 * - 用户名不存在
 * - 密码错误
 * - 其他认证异常
 * 
 * 返回格式：
 * {
 *   "code": 500,
 *   "msg": "登录失败",
 *   "data": null
 * }
 * 
 * 注意：
 * - 不应该在错误信息中暴露具体原因（如"用户名不存在"）
 * - 防止黑客利用错误信息进行用户枚举攻击
 * - 本示例返回通用错误信息 "登录失败"
 */
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {
    
    /**
     * 处理认证失败事件
     * 
     * @param request HTTP 请求
     * @param response HTTP 响应，用于返回错误信息
     * @param exception 认证异常，包含失败原因详情
     * @throws IOException 写入响应时可能抛出
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // 1. 构建错误响应对象
        // 仅返回通用错误信息，不暴露具体失败原因
        Result<String> error = Result.error("登录失败");
        
        // 2. 设置响应状态码和内容类型
        // 注意：使用 200 而非 401，与前端约定
        // 实际应用中可根据需要调整为标准 HTTP 状态码
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        
        // 3. 将错误对象序列化为 JSON 并写入响应流
        response.getWriter().write(JSONUtil.toJsonStr(error));
        
        // 可选：添加日志或统计
        // logger.warn("Authentication failed: {}", exception.getMessage());
    }
}
