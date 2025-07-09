package com.demo.controller;

import com.demo.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * @author : Tomatos
 * @date : 2025/7/9
 */
// @Tag - 配置UI界面相关信息
@Tag(name = "Hello模块", description = "Hello模块相关描述")
@RestController
@RequestMapping("/hello")
public class HelloController {
    @Operation(
            summary = "Hello Method summary",
            description = "Hello Method description"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "请求成功"
            ),
            // 设置content为空, 否则会展示相关值
            @ApiResponse(
                    responseCode = "401",
                    description = "未授权",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "禁止访问",
                    content = @Content()
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "未找到",
                    content = @Content()
            )
    })
    @GetMapping("call")
    public UserDTO hello() {
        return new UserDTO("Tom", 20);
    }

    @Operation(
            summary = "添加用户",
            description = "addUser method description",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "requestBody 描述"
            )
    )
    @PostMapping("add")
    public void addUser(
            @RequestBody
            UserDTO userDTO,
            Integer Other
    ) {

    }

    // 下面两个方法几乎等价, 如果使用@RequestParam, SpringBoot会自动去配置OpenApi
    // value 对应 description, defaultValue 对应 example
    @PutMapping("userid")
    public void addUserId(@RequestParam(value = "id", defaultValue = "99") Long i ){
    }

    @PutMapping("userid0")
    public void addUserId0(
            @Parameter(description = "id", required = true, example = "99")
            Long id
    ) {

    }

    @Operation(summary = "批量参数说明示例")
    @Parameters({
        // name指定的值和@RequestParam自动生成的信息的name值一致(id和name), @Parameter设置的其他值
        // (description, required..)会覆盖掉@RequestParam自动生成的值
        @Parameter(name = "id", description = "用户ID", required = true, example = "5"),
        @Parameter(name = "name", description = "用户名", required = false, example = "Tom")
    })
    @GetMapping("/user")
    public String getUser(@RequestParam Integer id, @RequestParam String name) {
        return "User: " + id + ", Name: " + name;
    }
}