package com.example.swagger3.controller.basic;

import com.example.swagger3.dto.Result;
import com.example.swagger3.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic")
@Tag(name = "basic", description = "基础示例接口")
public class BasicController {

    @GetMapping("/ping")
    @Operation(summary = "Ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by id")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UserDto.class)))
    public Result<UserDto> getUser(@PathVariable Long id) {
        UserDto user = new UserDto(id, "alice", "alice@example.com");
        return new Result<>(200, "success", user);
    }

    @PostMapping("/users")
    @Operation(summary = "Create user")
    public Result<UserDto> createUser(@RequestBody UserDto dto) {
        dto.setId(123L);
        return new Result<>(200, "created", dto);
    }
}
