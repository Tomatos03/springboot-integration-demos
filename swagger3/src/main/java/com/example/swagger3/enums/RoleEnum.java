package com.example.swagger3.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户角色", example = "USER")
public enum RoleEnum {
    @Schema(description = "管理员")
    ADMIN,
    @Schema(description = "普通用户")
    USER,
    @Schema(description = "访客")
    GUEST
}
