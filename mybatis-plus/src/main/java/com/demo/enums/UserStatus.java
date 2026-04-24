package com.demo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举
 * 使用 @JsonValue 和 @JsonCreator 简化 JSON 序列化/反序列化
 *
 * @author Tomatos
 * @date 2025/11/5
 */
@Getter
public enum UserStatus {

    /**
     * 活跃用户
     */
    ACTIVE(1, "活跃", "active"),

    /**
     * 非活跃用户
     */
    INACTIVE(2, "非活跃", "inactive"),

    /**
     * 被封禁用户
     */
    BLOCKED(3, "被封禁", "block");

    @EnumValue
    private final Integer code;
    private final String description;
    @JsonValue
    private final String key;

    UserStatus(Integer code, String description, String key) {
        this.code = code;
        this.description = description;
        this.key = key;
    }

    /**
     * @JsonCreator 注解：指定 JSON 反序列化时的转换方法
     * 支持通过编码（1, 2, 3）或名称（ACTIVE, INACTIVE, BLOCKED）来反序列化
     */
    @JsonCreator
    public static UserStatus fromJson(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        value = value.trim();

        // 先尝试按编码转换（数字）
        try {
            Integer code = Integer.parseInt(value);
            return getByCode(code);
        } catch (NumberFormatException e) {
            // 忽略，继续尝试按名称转换
        }

        // 再尝试按名称转换（ACTIVE, INACTIVE, BLOCKED）
        return getByName(value);
    }

    /**
     * 根据编码获取枚举
     */
    public static UserStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid UserStatus code: " + code);
    }

    /**
     * 根据名称获取枚举
     */
    public static UserStatus getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return UserStatus.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UserStatus name: " + name);
        }
    }
}
