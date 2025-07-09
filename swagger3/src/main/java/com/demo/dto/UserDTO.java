package com.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author : Tomatos
 * @date : 2025/7/9
 */
// 如果当前描述的实体类没有被其他已经标记到SwaggerUI的方法调用,那么就不会进行展示
@Schema(description = "用户传输对象")
@Getter
@Setter
@AllArgsConstructor
public class UserDTO {
    @Schema(description = "用户名", example = "Tom")
    private String name;
    @Schema(description = "年龄", example = "1")
    private int age;
}
