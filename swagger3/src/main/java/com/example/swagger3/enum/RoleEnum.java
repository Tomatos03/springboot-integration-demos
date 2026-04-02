package com.example.swagger3.enum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户角色")
public enum RoleEnum {
    ADMIN,
    USER,
    GUEST
}
