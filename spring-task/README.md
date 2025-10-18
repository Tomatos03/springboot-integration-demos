# Spring-Task
Spring Task 是 Spring Boot 提供的定时任务调度模块，支持基于注解的定时任务开发。它可以帮助开发者轻松实现定时任务的创建、管理与执行，常用于定时数据同步、定期清理等场景。
本项目基于 Spring Boot，集成了 Spring Task，演示了如何配置和使用定时任务。

## 使用步骤
1. 在 `pom.xml` 中添加 Spring Task 相关依赖:
> [!NOTE]
> Spring Task 被包含在`spring-context`这个maven包中

```xml
<!-- Spring项目  -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>5.3.20</version>
</dependency>

<!-- Spring Boot 基础依赖，包含spring-context -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>

<!-- Spring Boot Web依赖, 包含spring-boot-starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

2. 在主类或配置类上添加 `@EnableScheduling` 注解以启用定时任务功能。

```java
@EnableScheduling // 添加时间表
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

3. 创建定时任务类，并在方法上添加 `@Scheduled` 注解，配置定时规则。

```java
@Component
public class TimeTask {
    @Scheduled(fixedRate = 3000) // 单位毫秒
    public void printTime() {
        System.out.println(System.currentTimeMillis());
    }
}
```
常用的 `@Scheduled` 注解属性说明：

- `fixedRate`：按固定频率执行（上一次开始执行时间后间隔多久再次执行，单位毫秒）。
- `fixedDelay`：按固定延迟执行（上一次执行结束后间隔多久再次执行，单位毫秒）。
- `initialDelay`：首次延迟多久后执行（单位毫秒），可与 `fixedRate` 或 `fixedDelay` 配合使用。
- `cron`：使用 cron 表达式自定义复杂的定时规则。

示例：
```java
@Scheduled(cron = "0 0/5 * * * ?") // 每5分钟执行一次
public void task1() {
    System.out.println("task1...");
}

@Scheduled(fixedDelay = 10000, initialDelay = 5000) // 启动5秒后执行，之后每次结束后延迟10秒再执行
public void task2() {
    System.out.println("task2...");
}
```

4. 启动应用，定时任务会按配置自动执行。
