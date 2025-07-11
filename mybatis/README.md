# MyBatis Demo 使用说明

本模块用于演示如何在 Spring Boot 项目中集成和使用 MyBatis。


## 可选步骤

1. 在数据库UI工具中, 执行项目根目录下`demo-resource`目录的SQL文件

## 基本步骤

1. 添加依赖 (参考本项目的`pom.xml`)
2. 配置数据库信息 (参考本项目的`application.xml`)
3. 配置 Mybatis 信息 (参考本项目的`application.xml`)
4. 创建 Mapper 接口 (参考本项目的`com.demo.mapper`包)
5. 创建 Mapper 接口类对应的 XML 文件 (参考本项目的`resources/com.demo.mapper`目录)
6. 从 SpringBoot 容器中获取 Mapper 接口实现的 Bean (参考本项目的`Application.java`中的示例代码)
7. 调用 Mapper 接口中定义的方法进行操作数据库

## 参考

-   [MyBatis 官方文档](https://mybatis.org/mybatis-3/zh/index.html)
-   [MyBatis-Spring-Boot-Starter](https://github.com/mybatis/spring-boot-starter)
