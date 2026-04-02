package com.example.swagger3.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserDto", description = "用户示例实体")
public class UserDto {
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "alice")
    private String username;

    @Schema(description = "邮箱", example = "alice@example.com")
    private String email;

    public UserDto() {}

    public UserDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
