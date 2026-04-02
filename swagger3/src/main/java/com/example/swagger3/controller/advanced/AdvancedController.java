package com.example.swagger3.controller.advanced;

import com.example.swagger3.dto.Result;
import com.example.swagger3.enum.RoleEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/advanced")
@Tag(name = "advanced", description = "进阶功能示例")
public class AdvancedController {

    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件示例",
            requestBody = @RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)))
    public Result<String> upload(MultipartFile file) {
        // demo: 不保存文件，仅返回文件名
        return new Result<>(200, "ok", file.getOriginalFilename());
    }

    @PostMapping("/role")
    @Operation(summary = "枚举参数示例")
    public Result<RoleEnum> role(RoleEnum role) {
        return new Result<>(200, "ok", role);
    }
}
