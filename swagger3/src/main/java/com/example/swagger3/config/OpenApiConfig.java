package com.example.swagger3.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置类
 *
 * 本配置定义了 Swagger/OpenAPI 文档的全局设置，包括：
 * 1. 安全认证方案（如 Bearer Token）
 * 2. API 分组（将不同功能的接口分到不同的组中）
 */
@Configuration
public class OpenApiConfig {

    /**
     * 自定义 OpenAPI 配置
     *
     * 定义全局的安全认证方案。所有标记了 @SecurityRequirement 注解的接口
     * 都将在 Swagger UI 的 Authorize 按钮中显示此认证方式。
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 定义安全认证方案
                .components(new Components()
                        // 添加 Bearer Token 认证方案
                        // type: 认证类型为 HTTP
                        // scheme: 使用 bearer token 方式
                        // bearerFormat: 标记为 JWT 格式（便于文档说明）
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                )
                // 设置全局的默认安全要求
                // 这意味着大多数接口默认需要 bearerAuth 认证
                // 不需要认证的接口可以单独使用 @SecurityRequirement 覆盖
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * 基础功能 API 分组
     *
     * 将 basic 包下的所有 Controller 分组为 "basic"
     * 在 Swagger UI 中会显示为一个单独的选项卡
     */
    @Bean
    public GroupedOpenApi basicApi() {
        return GroupedOpenApi.builder()
                .group("basic")                                    // 分组名称（显示在 UI 中）
                .packagesToScan("com.example.swagger3.controller.basic")  // 扫描此包下的所有 Controller
                .build();
    }

    /**
     * 公开接口 API 分组
     *
     * 将 pub 包下的所有 Controller 分组为 "public"
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("com.example.swagger3.controller.pub")
                .build();
    }

    /**
     * 安全认证 API 分组
     *
     * 将 security 包下的所有 Controller 分组为 "security"
     * 用于演示 Bearer Token 认证的使用
     */
    @Bean
    public GroupedOpenApi securityApi() {
        return GroupedOpenApi.builder()
                .group("security")
                .packagesToScan("com.example.swagger3.controller.security")
                .build();
    }

    /**
     * 管理员接口 API 分组
     *
     * 将 admin 包下的所有 Controller 分组为 "admin"
     * 这些接口需要 ROLE_ADMIN 角色
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .packagesToScan("com.example.swagger3.controller.admin")
                .build();
    }

    /**
     * 进阶功能 API 分组
     *
     * 将 advanced 包下的所有 Controller 分组为 "advanced"
     * 用于演示文件上传、枚举参数等高级特性
     */
    @Bean
    public GroupedOpenApi advancedApi() {
        return GroupedOpenApi.builder()
                .group("advanced")
                .packagesToScan("com.example.swagger3.controller.advanced")
                .build();
    }
}
