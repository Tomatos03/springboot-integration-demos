package com.example.common.utils;

import com.example.common.exception.BusinessException;

import java.util.Collection;
import java.util.Objects;

public class Assert {

    private Assert() {}

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new BusinessException(message);
        }
    }

    public static void isTrue(boolean expression, Integer code, String message) {
        if (!expression) {
            throw new BusinessException(code, message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new BusinessException(message);
        }
    }

    public static void isFalse(boolean expression, Integer code, String message) {
        if (expression) {
            throw new BusinessException(code, message);
        }
    }

    public static void notNull(Object obj, String message) {
        if (Objects.isNull(obj)) {
            throw new BusinessException(message);
        }
    }

    public static void notNull(Object obj, Integer code, String message) {
        if (Objects.isNull(obj)) {
            throw new BusinessException(code, message);
        }
    }

    public static void isNull(Object obj, String message) {
        if (Objects.nonNull(obj)) {
            throw new BusinessException(message);
        }
    }

    public static void isNull(Object obj, Integer code, String message) {
        if (Objects.nonNull(obj)) {
            throw new BusinessException(code, message);
        }
    }

    public static void notEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(String str, Integer code, String message) {
        if (str == null || str.isEmpty()) {
            throw new BusinessException(code, message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(message);
        }
    }

    public static void notBlank(String str, Integer code, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(code, message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, Integer code, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BusinessException(code, message);
        }
    }

    public static void equals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new BusinessException(message);
        }
    }

    public static void notEquals(Object expected, Object actual, String message) {
        if (Objects.equals(expected, actual)) {
            throw new BusinessException(message);
        }
    }
}