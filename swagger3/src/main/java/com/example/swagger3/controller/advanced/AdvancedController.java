package com.example.swagger3.controller.advanced;

import com.example.swagger3.dto.Result;
import com.example.swagger3.enums.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/advanced")
@Tag(name = "advanced", description = "进阶功能示例")
public class AdvancedController {

    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件示例", description = "演示如何在Swagger UI中上传文件")
    @ApiResponse(responseCode = "200", description = "上传成功，返回文件名")
    public Result<String> upload(
            @Parameter(description = "上传的文件", required = true)
            @RequestParam("file") MultipartFile file) {
        return new Result<>(200, "ok", file.getOriginalFilename());
    }

    @PostMapping("/role")
    @Operation(summary = "枚举参数示例", description = "演示如何传递枚举类型参数")
    @ApiResponse(responseCode = "200", description = "返回收到的角色信息")
    public Result<RoleEnum> role(
            @Parameter(description = "用户角色", example = "USER", required = true)
            @RequestParam("role") RoleEnum role) {
        return new Result<>(200, "ok", role);
    }
}
