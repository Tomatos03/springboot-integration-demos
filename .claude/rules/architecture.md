# 架构与模块地图

## Reactor 结构

根 `pom.xml`（`packaging=pom`，父工程 `spring-boot-starter-parent:3.5.3`）聚合所有 Java 模块；**新增/删除模块必须同步维护根 pom 的 `<modules>`**。

模块包名不统一（`com.demo`、`org.demo`、`com.example` 都有），各模块以自身 pom 与 `application.yml` 为准，**不要假设存在全局统一的基础包**。

## 顶层模块

### 数据访问 / ORM

| 模块 | 技术 / 说明 |
|---|---|
| `mybatis` | MyBatis XML mapper 示例，连 MariaDB（库 `mybatis_demo`，凭据在 `application.yaml`）；SQL 参考 `demo-resource/` |
| `mybatis-plus` | MyBatis-Plus（boot3 starter + jsqlparser 分页/插件）；H2 内存库 + `schema.sql`/`data.sql`，演示逻辑删除、枚举转换 |
| `mybatis-generator` | MyBatis Generator 代码生成（**非 Web 应用**）：`mvn mybatis-generator:generate`，或直接运行 `MBGGenerator` 的 main |
| `spring-jpa-demo` | Spring Data JPA；H2 内存库，实体关联/Repository/分页/事务；`/h2-console` |

### 消息 / 缓存 / 定时

| 模块 | 技术 / 说明 |
|---|---|
| `rabbitmq` | 聚合模块：`producer`、`consumer` 两个 AMQP 应用（**无 Web 端口**），连本机 RabbitMQ vhost `my_vhost`，演示死信、延迟、发布确认、消费重试等 |
| `redis` | Spring Data Redis + Redisson：数据结构操作、缓存、分布式锁、限流、延时任务、Session 共享；默认 8082，`application-cluster.yml` 提供集群 profile |
| `spring-task` | `@Scheduled` 定时任务 + `@Async` 异步任务 + 自定义线程池 |

### 安全 / 认证

| 模块 | 技术 / 说明 |
|---|---|
| `Jwt` | JJWT + HandlerInterceptor 的手写 Token 鉴权 demo（无 Spring Security） |
| `spring-security` | 聚合模块：`common`（共享的 Security 配置/Result/异常处理器）+ 4 个独立子应用 —— `basic-auth`、`form-auth`（登录签发 JWT 存 Redis）、`sms-auth`（手机号+验证码，Redis）、`oauth2-auth`（GitHub 第三方登录）。**每个子应用都依赖兄弟模块 `common`**，单独运行前需先把 `common` 装进本地仓库 |

### API 文档 / 搜索 / 测试

| 模块 | 技术 / 说明 |
|---|---|
| `swagger3` | springdoc-openapi（Swagger UI `/swagger-ui/index.html`、OpenAPI JSON `/v3/api-docs`），带 Spring Security 与接口分组演示 |
| `elasticsearch` | ES Java Client 8.18.8 示例（CRUD、term/compound/fulltext/aggregation 查询、自动补全、高亮）；默认 8083；模块专属细节见其 `AGENTS.md` |
| `junit5-springboot3-demo` | JUnit 5 三种测试写法的样板模块：纯单测 / `@WebMvcTest` 切片 / `@SpringBootTest` 集成测试，新增测试可参考它 |

### 文档 / 办公

| 模块 | 技术 / 说明 |
|---|---|
| `fesode-excel` | POI + Apache Fesod（fesod-sheet）：单 Sheet 模板动态生成单文件多 Sheet Excel；**已关闭 spring-boot repackage**，产物是普通 jar，可被 `excel-to-pdf` 作为依赖复用 |
| `excel-to-pdf` | **依赖兄弟模块 `fesode-excel`**：复用其 ExcelUtil 生成多 Sheet Excel → `PDFUtil`（util 包静态工具）以进程调用 LibreOffice headless 转 PDF（分页遵循源 xlsx 每个 Sheet 的打印设置，不强制作一页一 Sheet）；默认 8080，运行前需装 LibreOffice（见模块 README） |

### Spring Cloud Alibaba（嵌套聚合）

`spring-cloud-alibaba-demo/` 内部再聚合 `alibaba-common`（共享模型与异常）、`alibaba-gateway`（网关，端口 8888）、`cloud-service`（三个 Dubbo 服务）。业务链路：Gateway 路由 → Dubbo 服务 → MySQL，并集成 Seata（分布式事务）、Sentinel（流控）、SkyWalking（链路追踪）。

- 三个 service（`account-service`/`order-service`/`storage-service`，端口 8081/8082/8083）都依赖 `alibaba-common`。
- **当前注册中心/服务发现用 Zookeeper**：Dubbo registry 与 Spring Cloud discovery 均指向 `zookeeper://127.0.0.1:2181`，Nacos 相关依赖/配置已注释。README 与控制台地址介绍可能仍写 Nacos，**以代码与 `docs/docker/.env` 为准**。

## 端口与基础设施速查

| 运行单元 | 默认端口 |
|---|---|
| swagger3 / spring-task / mybatis / mybatis-plus / Jwt / form-auth / sms-auth / oauth2-auth / fesode-excel / excel-to-pdf | 8080（多个同为 8080 的 demo 不可同时运行） |
| basic-auth / account-service | 8081 |
| redis / order-service | 8082 |
| elasticsearch / storage-service | 8083 |
| spring-jpa-demo | 7777 |
| alibaba-gateway | 8888 |
| rabbitmq producer / consumer | 无 Web 端口（纯 AMQP） |

> 端口以各模块 `application.yml` 为准，README 可能滞后。

Docker 编排位置：

- `elasticsearch/docs/docker/docker-compose.yml`：ES 8.18.8 + Kibana（`localhost:9200`）
- `spring-cloud-alibaba-demo/docs/docker/compose.yml`（配套 `.env`）：MySQL、Zookeeper、Seata、SkyWalking（Nacos 已注释，可选启用）
- `excel-to-pdf/docs/docker/`：Dockerfile + compose，构建「JDK + LibreOffice」镜像直接运行应用（构建上下文是仓库根目录）
- 其它模块默认假设本机已有中间件；如需容器化，沿用 `<module>/docs/docker/compose.yml` 的既有模式。
