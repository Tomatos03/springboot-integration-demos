package com.demo.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author : Tomatos
 * @date : 2025/7/17
 */
@Data
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T>  success(String message, T data) {
        return new Result<>(1, message, data);
    }

    public static <T> Result<T>  success(T data) {
        return new Result<>(1, null, data);
    }

    public static <T> Result<T>  success() {
        return new Result<>(1, null, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(0, message, null);
    }
}
