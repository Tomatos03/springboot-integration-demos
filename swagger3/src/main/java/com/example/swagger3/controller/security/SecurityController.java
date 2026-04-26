package com.example.swagger3.controller.security;

import com.example.swagger3.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
@Tag(name = "security", description = "安全认证示例")
public class SecurityController {

    @GetMapping("/secured")
    @Operation(
            summary = "受保护接口示例",
            description = "需要Bearer Token认证才能访问，使用 Bearer demo-admin-token",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "认证成功，返回受保护数据")
    @ApiResponse(responseCode = "401", description = "未认证或认证失败")
    public Result<String> secured() {
        return new Result<>(200, "ok", "secured data");
    }
}
