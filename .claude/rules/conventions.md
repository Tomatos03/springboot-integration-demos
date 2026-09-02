# 编写 Demo 的约定

## 模块风格

- **一个模块一个主题**：每新增一个「框架/中间件集成」就新建一个顶层模块，不要往已有模块里塞不相关功能；同时把模块注册到根 `pom.xml` 的 `<modules>`。
- 包结构按分层组织：`config/`、`controller/`、`service/`（接口 + 实现）、`mapper/` 或 `repository/`、`entity/`、`dto/`、`handler/`。Controller 尽量薄，返回统一包装 `Result`/`ApiResponse<T>`（多数模块已有，先查找复用，不要各写一套）。
- 广泛使用 Lombok（`@Slf4j`、`@Getter/@Setter`、构造器注解）。
- Spring Boot 3.x 体系：`jakarta.*` 命名空间、测试用 `@MockitoBean`（**不是**已废弃的 `@MockBean`）、父版本 3.5.3。
- demo 需要数据库又不想让用户装外部服务时，优先 H2 内存库 + `schema.sql`/`data.sql` 自动初始化（参考 `mybatis-plus`、`spring-jpa-demo`）。

## 注释

- 代码注释不使用任何形式的序号（如 `// 1)`、`①`、javadoc 的 `<ol>` 有序列表）；需要表达步骤/层次时改用叙述句或 `<ul>` 无序列表。「第 N 行 / 第 N 段」这类位置描述不属于序号，不受此限。

## 基础设施

- 引入外部中间件的新 demo，在 `<module>/docs/docker/compose.yml` 提供编排，并在 README 里写清「运行前准备」。若只放本机自装服务的假设（如 Redis、RabbitMQ），也要在 README 显式说明版本与连接配置。
- 不要把个人凭据硬编码进新配置。历史模块（`mybatis` 的数据库口令、`sms-auth` 的 Redis 口令）留有硬编码值，**新代码不要沿用该习惯**。

## 文档

- 每个模块的 `README.md` 是中文教学文档，逐条讲设计要点并附接口/curl 示例。**改动模块行为或新增接口时同步更新它。**
- README 可能滞后于实际配置（典型如端口号、Nacos 与 Zookeeper 之争），有出入时**以代码与 `application.yml` / compose 配置为准**，不要照 README 改代码。
- 流程图/架构图（SVG）放在模块内 `assets/` 或 `docs/assets/`，README 通过相对路径引用。
- `elasticsearch/`、`elasticsearch-ui/` 两个模块的专属约定记在其 `AGENTS.md` 里，改动这两个模块时以它为准、不要重复写进根 CLAUDE.md。

## Git / 提交

- Conventional Commits：`type(module): 描述`，scope 用模块名（如 `mybatis-plus`、`spring-security`），描述用中文。
- 历史 commit 的 type 写作 `refractor`（拼写错误）与 `feat` 混用；新提交请用标准拼写 `refactor` 并保持 `feat`/`refactor`/`chore`/`docs` 等前缀。
