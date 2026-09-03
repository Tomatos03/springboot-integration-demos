# logging-file-demo

一个用于学习「Spring Boot 日志持久化到文件」的独立模块。

日常开发中,我们习惯用 Lombok 的 `@Slf4j` 把日志直接打印到控制台;到了生产环境,控制台日志既看不到历史、也不方便检索,通常需要把日志写入文件并按时按量滚动归档。这个 demo 演示如何**不改一行业务代码**,就让 `@Slf4j` 的日志同时输出到控制台和文件,并且给出两种配置方式:

- **方式一:纯属性配置**(`application.yml`,零 XML,适合 80% 场景)
- **方式二:XML 配置**(`logback-spring.xml`,适合需要精细控制「不同包写不同文件、异步日志」等复杂场景)

两种方式二选一,本文档分别讲解并给出验证方法。

## 配置方式

下面分别讲解两种配置方式,使用时二选一;启用 XML 方式时,`application.yml` 里的 `logging.*` 会全部失效。

### 纯属性配置(默认)

配置集中在 [`src/main/resources/application.yml`](src/main/resources/application.yml)。每个参数的含义都已用**注释写在配置里**,这里只讲三条主线。

1) 指定日志文件

```yaml
logging:
  file:
    name: logs/app.log
```

只要配置了 `logging.file.name`,Spring Boot 就会**同时**保留控制台输出与文件输出,业务代码不用任何改动。路径可以是绝对路径或相对「运行目录」的相对路径,目录不存在会自动创建。

> 补充:`logging.file.path` 只指定目录、文件名固定为 `spring.log`;`file.name` 优先于 `file.path`(详见配置文件注释)。

2) 日志滚动策略

```yaml
logging:
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
      total-size-cap: 300MB
      file-name-pattern: ${LOG_FILE}.%d{yyyy-MM-dd}.%i.log.gz
```

达到滚动条件后,当前 `app.log` 会被归档成 `app.log.2025-09-03.0.log.gz` 之类,最新日志继续写 `app.log`。每个参数都表示什么,看 `application.yml` 里的行内/块注释即可(默认值:`max-file-size` 10MB、`max-history` 7、`total-size-cap` 不限)。

3) 日志级别

```yaml
logging:
  level:
    root: INFO                                  # 全局默认级别
    org.demo.logging.controller: INFO           # 按包/类单独设置, 包级别优先级高于 root
```

级别从低到高:`TRACE < DEBUG < INFO < WARN < ERROR`,只会输出「大于等于」所配级别的日志。想看到 DEBUG 日志,两种方式:

- 临时把 `org.demo.logging.controller` 改成 `DEBUG`
- 或使用本模块附带的 `debug` profile(已把该包级别调为 `DEBUG`):`java -jar ... --spring.profiles.active=debug`

> **💡 Tip:** 生产环境不建议把 root 调到 DEBUG,日志量会爆炸;按包/按类精确放行才是可维护的做法。

### XML 配置

**为什么还要 XML?**

`application.yml` 的属性方式适合常规需求;但当你要「不同包写不同文件」「按天/按月归档」「给某类日志单独异步输出」等更细的控制时,属性配置就不够用了,需要直接上手 Logback 的 XML。

#### 配置文件

[`src/main/resources/xml/logback-spring.xml`](src/main/resources/xml/logback-spring.xml) 是一份**完整可用**的 XML 日志配置,结构与每个元素的作用都写在文件注释里。核心结构:

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">...</appender>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>...</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>...</fileNamePattern>
            <maxFileSize>10MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>300MB</totalSizeCap>
        </rollingPolicy>
    </appender>
    <logger name="org.demo.logging.controller" level="INFO"/>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

#### 为什么文件名是 logback-spring.xml 而不是 logback.xml

Spring Boot 对各日志系统有「配置文件放到指定位置即自动生效」的默认约定:把配置文件放进 classpath 根目录(即 `src/main/resources/`),**无需任何启动参数**就会被自动加载:

| 日志系统 | 放在 classpath 根目录即自动加载的文件名 |
|---|---|
| Logback | `logback-spring.xml`、`logback-spring.groovy`、`logback.xml`、`logback.groovy` |
| Log4j2 | `log4j2-spring.xml`、`log4j2.xml` |
| JDK (Java Util Logging) | `logging.properties` |

这份约定还有两个注意点:

- 只有文件名带 `-spring`(即 `logback-spring.xml`)时,才支持在 XML 里使用 Spring 的扩展能力(`<springProperty>`、`<springProfile>`、`${LOG_FILE}` 等属性注入);若用纯 `logback.xml`,则无法使用这些 Spring 特性。Spring Boot 官方建议优先使用 `-spring` 变体,否则框架无法完全控制日志初始化。
- 显式设置 `logging.config`(如 `--logging.config=classpath:xml/logback-spring.xml`)**优先于**默认约定;生产环境想用 jar 外部的日志配置时,可指向 `file:` 路径(如 `--logging.config=file:/opt/app/logback-spring.xml`)。

本 demo 刻意把 XML 放在 `src/main/resources/xml/` **子目录**而非 classpath 根目录,使其不满足上述「根目录默认命名」约定、不会被自动加载——由你显式指定,避免和方式一的 `application.yml` 冲突。


## 运行与验证

```bash
# 在仓库根目录编译(不跑测试)
mvn -pl logging-file-demo -am clean package -DskipTests

# 方式一: 直接运行(application.yml)
cd logging-file-demo && mvn spring-boot:run

# 方式二: 用 XML 配置运行(logback-spring.xml)
cd logging-file-demo && mvn spring-boot:run -Dspring-boot.run.arguments=--logging.config=classpath:xml/logback-spring.xml
```

启动完成后,访问以下接口触发日志:

```bash
# 输出 5 个级别的日志各一条
curl http://localhost:8080/log/demo

# 带占位符参数的订单流程日志(重点看参数拼接与检索)
curl "http://localhost:8080/log/order?orderId=1001"

# 批量刷日志,验证滚动策略(默认 10 万条, 可手动加大撑到 10MB)
curl "http://localhost:8080/log/loop?times=100000"
```

- 方式一:日志出现在 `logs/app.log`,滚动后产生 `app.log.2025-09-03.0.log.gz` 之类的归档
- 方式二:日志出现在 `logs/app-xml.log`,滚动后产生 `app-xml.log.2025-09-03.0.log.gz`

---

## 常见问题

### 程序启动参数配置了 `logging.file.name` 却没有文件?

程序参数 `--logging.config`。一旦指定 XML,Spring Boot 就以 XML 为准,`application.yml` 里的 `logging.*` 全部失效。

### 两种方式能同时生效吗?

不能。要么用 `application.yml` 的属性,要么用 `--logging.config` 指定的 XML,二选一。方式二启用时日志输出完全由 XML 控制。
