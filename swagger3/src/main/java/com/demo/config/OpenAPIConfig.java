package com.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Tomatos
 * @date : 2025/7/9
 */
// 通过注解配置Swagger3与下面通过注册Bean配置网页元数据的方式相同
// 注解对应 io.swagger.v3.oas.annotations.info. 包
// 模型对应 io.swagger.v3.oas.models.info. 包
@OpenAPIDefinition(
        info = @Info(
                title = "示例Demo-Title",
                description = "This is demo description",
                version = "v1.0",
                contact = @Contact(
                        name = "name: Daming",
                        email = "111@qq.com",
                        url = "www.baidu.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        )
)
@Configuration
public class OpenAPIConfig {
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                              .title("示例Demo-Title")
//                              .description("This is demo description")
//                              .version("v1.0")
//                              .contact(new Contact()
//                                               .name("Daming")
//                                               .email("111@qq.com")
//                                               .url("www.baidu.com")
//                              )
//                              .license(new License()
//                                               .name("Apache 2.0")
//                                               .url("https://www.apache.org/licenses/LICENSE-2.0.html")
//                              )
//                );
//    }
}