# Swagger3 (springdoc-openapi) 使用说明

本模块用于集成和演示如何在 Spring Boot 项目中使用 springdoc-openapi（Swagger3/OpenAPI 3）进行接口文档自动生成。

## 快速开始

1. **添加依赖**

    在 `pom.xml` 中添加如下依赖：

    ```xml
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.8.9</version>
    </dependency>
    ```

2. **零配置使用**

    springdoc-openapi 支持开箱即用，通常无需额外配置。只需保证你的 Controller 使用了标准的 Spring Web 注解（如 `@RestController`、`@RequestMapping` 等）。

3. **访问接口文档**

    启动 Spring Boot 项目后，访问：

    - [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    - [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

4. **自定义配置（可选）**

    如果需要自定义 OpenAPI 文档的元数据，可以在 SpringBoot 加载的默认配置文件 `application.yml` 中配置 springdoc 提供的部分属性, 并配合 Swagger 提供的注解定义 Swagger-UI 网页相关内容

## 参考

-   [springdoc-openapi 官方文档](https://springdoc.org/)
-   [OpenAPI 规范](https://swagger.io/specification/)

如需分组、接口描述等高级用法，请参考官方文档。
