package com.example.swagger3.controller.pub;

import com.example.swagger3.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@Tag(name = "public", description = "公开接口示例")
public class PublicController {

    @GetMapping("/info")
    @Operation(summary = "获取公开信息", description = "无需认证，任何人都可以访问")
    @ApiResponse(responseCode = "200", description = "返回公开信息")
    public Result<String> info() {
        return new Result<>(200, "ok", "public info");
    }
}
