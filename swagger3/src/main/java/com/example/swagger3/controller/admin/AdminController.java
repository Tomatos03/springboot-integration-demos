package com.example.swagger3.controller.admin;

import com.example.swagger3.dto.Result;
import com.example.swagger3.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "admin", description = "管理接口示例")
public class AdminController {

    @GetMapping("/me")
    @Operation(summary = "当前管理员信息", security = @SecurityRequirement(name = "bearerAuth"))
    public Result<UserDto> me() {
        UserDto user = new UserDto(1L, "admin", "admin@example.com");
        return new Result<>(200, "ok", user);
    }
}
