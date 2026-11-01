package org.demo.jwt.dual.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.demo.jwt.dual.dto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 把 {@link AuthException} 转换为统一响应结构，并沿用异常携带的 HTTP 状态码。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public Result<Void> handleAuthException(AuthException ex, HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        return Result.failure(ex.getBizCode(), ex.getMessage());
    }
}
