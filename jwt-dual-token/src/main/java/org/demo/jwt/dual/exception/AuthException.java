package org.demo.jwt.dual.exception;

import lombok.Getter;
import org.demo.jwt.dual.dto.BizCode;
import org.springframework.http.HttpStatus;

/**
 * 认证相关业务异常，由全局异常处理器转换为统一响应结构。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@Getter
public class AuthException extends RuntimeException {

    private final HttpStatus status;
    private final BizCode bizCode;

    public AuthException(HttpStatus status, BizCode bizCode) {
        super(bizCode.message());
        this.status = status;
        this.bizCode = bizCode;
    }

    public AuthException(HttpStatus status, BizCode bizCode, String message) {
        super(message);
        this.status = status;
        this.bizCode = bizCode;
    }

    public static AuthException unauthorized(BizCode bizCode) {
        return new AuthException(HttpStatus.UNAUTHORIZED, bizCode);
    }

    public static AuthException unauthorized(BizCode bizCode, String message) {
        return new AuthException(HttpStatus.UNAUTHORIZED, bizCode, message);
    }
}
