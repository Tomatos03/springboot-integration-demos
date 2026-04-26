package com.example.swagger3.controller.basic;

import com.example.swagger3.dto.Result;
import com.example.swagger3.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic")
@Tag(name = "basic", description = "基础示例接口")
public class BasicController {

    @GetMapping("/ping")
    @Operation(summary = "Ping测试", description = "用于测试服务是否正常运行")
    @ApiResponse(responseCode = "200", description = "返回pong")
    public String ping() {
        return "pong";
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "根据ID查询用户", description = "通过用户ID获取用户详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "ID参数无效")
    })
    public Result<UserDto> getUser(
            @Parameter(description = "用户ID", example = "1", required = true)
            @PathVariable Long id) {
        UserDto user = new UserDto(id, "alice", "alice@example.com");
        return new Result<>(200, "success", user);
    }

    @PostMapping("/users")
    @Operation(summary = "创建新用户", description = "创建一个新的用户记录")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功",
                    content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "400", description = "请求参数无效")
    })
    public Result<UserDto> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "用户信息",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            )
            @RequestBody UserDto dto
    ) {
        dto.setId(123L);
        return new Result<>(200, "created", dto);
    }
}
