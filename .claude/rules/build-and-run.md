# 构建 / 运行 / 测试

环境：JDK 17 + Maven。以下命令默认在**仓库根目录**执行；嵌套模块的路径形如 `spring-security/form-auth`、`spring-cloud-alibaba-demo/cloud-service/order-service`。

## 构建

```bash
# 全量构建 reactor（跳测试：部分模块测试依赖外部中间件）
mvn -DskipTests clean install

# 只构建某个模块及其兄弟依赖（-am 会把同 reactor 的依赖一起构建）
mvn -pl mybatis-plus -am clean package -DskipTests
mvn -pl spring-security/form-auth -am clean package -DskipTests            # 顺带构建 common
mvn -pl excel-to-pdf -am clean package -DskipTests                         # 顺带构建 fesode-excel
mvn -pl spring-cloud-alibaba-demo/alibaba-gateway -am clean package -DskipTests
```

## 运行单个 demo

有兄弟模块依赖的应用（spring-security 的子应用、cloud 的三个 service、rabbitmq 的子应用）需先安装依赖模块到本地仓库：

```bash
# 以 form-auth 为例：先装 common，再运行
mvn -pl spring-security/common -am install -DskipTests
mvn -pl spring-security/form-auth spring-boot:run -DskipTests        # 或 cd 进模块目录后 mvn spring-boot:run

# excel-to-pdf：先装 fesode-excel（普通 jar），再运行（需本机装 LibreOffice，见模块 README）
mvn -pl fesode-excel -am install -DskipTests
mvn -pl excel-to-pdf spring-boot:run -DskipTests
```

无兄弟依赖的模块直接运行：

```bash
cd elasticsearch && mvn spring-boot:run      # 先 docker compose 起 ES
cd swagger3 && mvn spring-boot:run
```

> 多个 demo 默认端口可能都是 8080，运行前先确认目标模块的 `server.port`（见 @rules/architecture.md 速查表）。

## Spring Cloud Alibaba demo（一键闭环）

用 `spring-cloud-alibaba-demo/docs/scripts/skywalking-closure.sh`（需要 `docs/docker/.env` 存在）：

```bash
cd spring-cloud-alibaba-demo
./docs/scripts/skywalking-closure.sh start     # 起基础设施(compose) + 打包 + 启动网关与三服务 + 触发一次闭环请求
./docs/scripts/skywalking-closure.sh verify    # 仅再触发闭环请求
./docs/scripts/skywalking-closure.sh status
./docs/scripts/skywalking-closure.sh stop
```

## 测试

```bash
# 跑某个模块全部测试
mvn -pl junit5-springboot3-demo test

# 单个测试类 / 单个方法（JUnit 5）
mvn -pl junit5-springboot3-demo -Dtest=DemoControllerWebMvcTest test
mvn -pl junit5-springboot3-demo -Dtest=DemoControllerWebMvcTest#方法名 test

# 需要兄弟模块时加 -am
mvn -pl mybatis-plus -am test
```

注意：仓库测试不多且分散。部分测试需要真实外部中间件（如 `elasticsearch` 的 `AutocompleteServiceTest` 需要 ES，`mybatis`/cloud service 的测试需要数据库，`excel-to-pdf` 的 `ExcelPdfManualCheckTest` 需要本机 LibreOffice、未安装会自动跳过）。本地未起中间件时用 `-DskipTests` 编译，或只跑不依赖中间件的模块（如 `junit5-springboot3-demo`、`Jwt`）。

## elasticsearch-ui（前端）

不在 Maven reactor 中，独立用 pnpm：

```bash
cd elasticsearch-ui
pnpm install
pnpm dev              # http://localhost:5173，开发代理指向后端 8083
pnpm type-check && pnpm build
```

## 基础设施

```bash
# Elasticsearch demo
cd elasticsearch/docs/docker && docker compose up -d

# Spring Cloud demo（.env 提供镜像标签与端口）
cd spring-cloud-alibaba-demo/docs/docker && docker compose --env-file .env up -d
```
