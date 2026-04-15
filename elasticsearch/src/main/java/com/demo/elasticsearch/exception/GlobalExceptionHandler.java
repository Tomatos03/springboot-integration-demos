package com.demo.elasticsearch.exception;

import com.demo.elasticsearch.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBizException(BizException ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(ex.getCode());
        response.setMessage(ex.getMessage());
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(it -> it.getField() + " " + it.getDefaultMessage())
                .orElse("参数校验失败");
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(400);
        response.setMessage(msg);
        return response;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        log.error("服务器内部异常: ", ex);
        response.setCode(500);
        response.setMessage(ex.getMessage());
        return response;
    }
}
