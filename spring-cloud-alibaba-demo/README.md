# spring-cloud-alibaba-demo

## 模块结构

```text
spring-cloud-alibaba-demo/
├── alibaba-common/      # 公共模型与异常封装
├── alibaba-gateway/     # 网关入口与路由转发
├── cloud-service/       # 业务服务（order/storage/account）
├── docs/
│   ├── sql/             # 初始化 SQL
│   ├── docker/          # 统一 Docker 编排与 .env
│   └── scripts/
│       └── skywalking-closure.sh  # 一键闭环启动脚本
```

## 总体架构图

![总体架构图](./docs/assets/architecture-overview.svg)

## 组件介绍

- **Nacos**：服务注册、服务发现、配置管理、配置热更新
- **Sentinel**：流量控制、熔断降级、热点参数限流、系统自适应保护
- **Seata**：分布式事务协调、AT 模式全局事务管理、全局锁控制
- **SkyWalking**：链路追踪、性能指标监控、服务拓扑分析、日志采集
- **Gateway**：请求路由、路径重写、统一鉴权、过滤器链、异常处理
- **MySQL**：数据持久化、事务存储

## 控制台访问地址

- `Nacos Console`：`http://127.0.0.1:18848/`（默认账号/密码 nacos/nacos）
- `Sentinel Dashboard`：`http://127.0.0.1:8080`（默认账号/密码 sentinel/sentinel）
- `SkyWalking UI`：`http://127.0.0.1:18080`（默认无需鉴权）

## 控制台登录说明

- 当前 `docs/docker/.env` 默认配置为 `NACOS_AUTH_ENABLE=false`，本地 demo 可直接访问 Nacos Console。
- 如果你改成 `NACOS_AUTH_ENABLE=true`，Nacos 默认账号密码是 `nacos/nacos`（首次登录建议修改）。
- Sentinel Dashboard 默认账号密码是 `sentinel/sentinel`。


## Gateway

### 请求路由

#### 配置

网关路由在 `application.yml` 中配置：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: account-route
          uri: lb://account-service
          predicates:
            - Path=/account/**
          filters:
            - StripPrefix=1
        - id: order-route
          uri: lb://order-service
          predicates:
            - Path=/order/**
          filters:
            - StripPrefix=1
        - id: storage-route
          uri: lb://storage-service
          predicates:
            - Path=/storage/**
          filters:
            - StripPrefix=1
```

配置说明：
- `id`：路由唯一标识
- `uri`：目标服务地址，`lb://` 表示使用负载均衡
- `predicates`：路由匹配规则，`Path=/account/**` 表示以 `/account` 开头的请求
- `filters`：过滤器链，`StripPrefix=1` 表示去掉请求路径的第一段（如 `/account/api` -> `/api`）

#### 验证

尝试发送以下任意请求，请求由网关统一转发到对应的服务之中.

```bash
curl -sS "http://127.0.0.1:8888/account/api/account/health"
curl -sS "http://127.0.0.1:8888/storage/api/storage/health"
curl -sS "http://127.0.0.1:8888/order/api/order/health"
```

```bash
curl -sS "http://127.0.0.1:8888/account/api/account/list"
curl -sS "http://127.0.0.1:8888/storage/api/storage/list"
curl -sS "http://127.0.0.1:8888/order/api/order/list"
```

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":2,"money":100.00}'
```

---

### 自定义断言

#### 配置

**1. 创建白名单路径断言类**

```java
 @Component
@Slf4j
public class WhitelistPathRoutePredicateFactory extends AbstractRoutePredicateFactory<WhitelistPathRoutePredicateFactory.Config> {

    public WhitelistPathRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new WhitelistPathPredicate(config);
    }

    // Config类之中的字段名称与当前方法声明的必须一致
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("whiteList");
    }

    // 将以逗号分割的参数收集成list
    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Setter
    @Getter
    public static class Config {
        private List<String> whiteList;
    }

    private static class WhitelistPathPredicate implements Predicate<ServerWebExchange> {
        private final Config config;

        public WhitelistPathPredicate(Config config) {
            this.config = config;
        }

        @Override
        public boolean test(ServerWebExchange exchange) {
            String path = exchange.getRequest().getURI().getPath();
            List<String> whiteList = config.getWhiteList();
            if (whiteList == null || whiteList.isEmpty()) {
                log.warn("Whitelist is empty, all paths will be allowed");
                return true;
            }
            return whiteList.stream().anyMatch(path::startsWith);
        }
    }
}
```

位置：`com.example.gateway.predicate.WhitelistPathRoutePredicateFactory`

**2. YAML 配置**

```yaml
- id: order-route
  uri: lb://order-service
  predicates:
    - Path=/order/**
    - WhitelistPath=/api/order/health,/api/order/list
  filters:
    - StripPrefix=1
    - RequestTimestamp
```

配置说明：
- `WhitelistPath`：指定允许访问的路径白名单，只有匹配的路径才能通过该路由
- 使用简写格式时，逗号分隔的值会通过 `GATHER_LIST` 模式收集为 List

**配置约定**

1. **断言名称映射**

   - 类名去掉 `RoutePredicateFactory` 后缀
   - `WhitelistPathRoutePredicateFactory` → `WhitelistPath`

2. **shortcutFieldOrder() 方法**

   - 返回 YAML 简写格式中参数与 Config 属性的映射关系
   - 例如：`return List.of("whiteList")` 表示 YAML 中第一个参数映射到 Config 的 `whiteList` 属性

3. **shortcutType() 方法**

   返回一个ShortcutType枚举，枚举值参考下表：

   | 枚举值 | 说明 |
   |--------|------|
   | `SHORTEST` | 根据shortcutFieldOrder方法声明的字段名称，通过反射将值赋予到Config类对应的成员变量上。如果找不到Config类对应的成员属性就直接跳过 |
   | `GATHER_LIST` | 要求 shortcutFieldOrder 大小为 1。将所有 value 收集为一个 List，放入 fieldOrder 指定的字段名下 |
   | `GATHER_LIST_TAIL_FLAG` | 要求 shortcutFieldOrder 大小为 2。最后一个 value 如果是 true/false/null，则作为布尔标志分离；其余 value 收集为 List |

4. **YAML 配置格式**

   **简写格式**（逗号分隔，默认使用逗号分割）

   ```yaml
   - WhitelistPath=/api/order/health,/api/order/list
   ```
   - 需要配合 `shortcutType() = GATHER_LIST` 使用
   - Gateway 将逗号分隔的值收集为 List，传入 Config 的 `whiteList` 属性

   **完整格式**（YAML List）

   ```yaml
   - name: WhitelistPath
     args:
       whiteList:
         - /api/order/health
         - /api/order/list
   ```
   - Gateway 将 YAML List 直接映射为 `List<String>` 传入

   > [!NOTE]
   >
   > 完整形式声明，不需要重写shortcutFieldOrder(), 如果使用简写形式就一定要

#### 验证

**验证白名单路径**

允许的路径可以直接访问：

```bash
curl -sS "http://127.0.0.1:8888/order/api/order/health"
```

预期：正常返回

**验证非白名单路径**

非白名单路径会被拒绝（404 或无匹配路由）：

```bash
curl -sS "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期：返回 404 或找不到路由

---

### 网关异常处理器

#### 配置

`alibaba-gateway` 配置了 `GatewayExceptionHandler` 全局异常处理器，统一处理网关层面的异常。

```java
@Slf4j
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(ex, status);
        exchange.getResponse().setStatusCode(status);
        // 返回统一格式的 JSON 响应
    }
}
```

处理规则：

| 触发场景 | 状态码 | message 来源 |
|---------|--------|-------------|
| 路由失败（服务不可用） | 500 | "网关服务异常" |
| 路径不存在（404） | 404 | "Not Found" |
| 网关内部异常 | 500 | "网关服务异常" |

#### 验证

**场景 1：路由失败（服务不可用）**

前置条件：order-service 未启动

```bash
curl -sS "http://127.0.0.1:8888/order/api/order/list"
```

预期返回：
```json
{"code":500,"message":"网关服务异常"}
```

**场景 2：路径不存在（404）**

```bash
curl -sS "http://127.0.0.1:8888/not-exist-path"
```

预期返回：
```json
{"code":404,"message":"Not Found"}
```

---

### 全局过滤器

#### 配置

`alibaba-gateway` 配置了两个全局过滤器（GlobalFilter），位于 `filter/global/` 目录下，作用于所有路由。

**GatewayRequestLogFilter**

```java
@Slf4j
@Component
public class GatewayRequestLogFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Request: {} {}", request.getMethod(), request.getPath());
        return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        log.info("Response: {} - Status: {}", ...);
                    }));
    }
}
```

位置：`com.example.gateway.filter.global.GatewayRequestLogFilter`

**GatewayTimingFilter**

```java
@Slf4j
@Component
public class GatewayTimingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(START_TIME_ATTR, System.nanoTime());
        return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        // 计算耗时并记录日志
                    }));
    }
}
```

位置：`com.example.gateway.filter.global.GatewayTimingFilter`

配置说明：
- `GlobalFilter` 是全局过滤器，作用于所有请求
- `@Component` 自动注册到 Gateway 过滤器链中

#### 验证

**验证 GatewayRequestLogFilter**

发送任意请求，检查网关日志输出：

```bash
curl -sS "http://127.0.0.1:8888/account/api/account/health"
```

预期：网关控制台日志显示类似如下内容：
```
Request: GET /account/api/account/health
Response: GET /account/api/account/health - Status: 200 OK
```

**验证 GatewayTimingFilter**

发送请求，检查耗时日志：

```bash
curl -sS "http://127.0.0.1:8888/order/api/order/health"
```

预期：网关控制台日志显示类似如下内容：
```
GET /order/api/order/health - 耗时: 0.xxxs
```

---

### 路由过滤器

#### 配置

`alibaba-gateway` 为 `order-route` 配置了路由级别的过滤器（GatewayFilter），位于 `filter/route/` 目录下，仅作用于特定路由。

**RequestTimestampFilter（实现 GatewayFilter 接口）**

```java
@Slf4j
@Component
public class RequestTimestampFilter implements GatewayFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-Timestamp", Instant.now().toString())
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
```

位置：`com.example.gateway.filter.route.RequestTimestampFilter`

**RequestTimestampFilterFactory（继承 AbstractGatewayFilterFactory）**

```java
@Slf4j
@Component
public class RequestTimestampFilterFactory extends AbstractGatewayFilterFactory<Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 添加时间戳 header
        };
    }

    public static class Config {
        private boolean enabled = true;
    }
}
```

位置：`com.example.gateway.filter.route.RequestTimestampFilterFactory`

路由配置（在 `application.yml` 中）：

```yaml
- id: order-route
  uri: lb://order-service
  filters:
    - StripPrefix=1
    - RequestTimestamp
```

配置说明：
- `GatewayFilter` 只作用于特定路由，需在路由配置中手动添加
- `AbstractGatewayFilterFactory` 是工厂类，配置时使用过滤器名称（如 `RequestTimestamp`）

#### 验证

**验证 RequestTimestampFilter**

发送请求到 order-service，检查响应 header 中是否包含时间戳：

```bash
curl -sS -i "http://127.0.0.1:8888/order/api/order/health"
```

预期：响应 header 中包含 `X-Request-Timestamp` 字段，格式类似：
```
X-Request-Timestamp: 2026-04-05T12:00:00.123Z
```

发送请求到其他路由（如 account-service），确认该过滤器仅作用于 `order-route`：

```bash
curl -sS -i "http://127.0.0.1:8888/account/api/account/health"
```

预期：响应 header 中**不包含** `X-Request-Timestamp` 字段。

---

### 全局CORS配置

#### 配置

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
```

配置说明：
- `setAllowCredentials(true)`：允许携带凭证（如 cookies）
- `addAllowedOriginPattern("*")`：允许所有来源（使用 pattern 而非 `*`，以支持 credentials）
- `addAllowedHeader("*")`：允许所有请求头
- `addAllowedMethod("*")`：允许所有 HTTP 方法
- `setMaxAge(3600L)`：预检请求缓存时间为 1 小时

#### 验证

**验证全局 CORS 配置**

发送 OPTIONS 预检请求：

```bash
curl -sS -i -X OPTIONS "http://127.0.0.1:8888/account/api/account/health" \
  -H "Origin: http://example.com" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type"
```

预期：响应 header 中包含：
```
Access-Control-Allow-Origin: http://example.com
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS
Access-Control-Allow-Headers: Content-Type
Access-Control-Max-Age: 3600
```

**验证跨域请求**

```bash
curl -sS -i "http://127.0.0.1:8888/account/api/account/health" \
  -H "Origin: http://example.com"
```

预期：响应 header 中包含 `Access-Control-Allow-Origin` 字段。

---

## Seata

### 跨服务回滚

#### 配置

Seata 分布式事务通过 `@GlobalTransactional` 注解配置，在 `OrderServiceImpl.createOrder` 方法上添加注解：

```java
@Override
@GlobalTransactional(name = "create-order-tx", rollbackFor = Exception.class)
@Transactional(rollbackFor = Exception.class)
public Long createOrder(CreateOrderRequest request) {
    // 调用库存服务扣减库存
    ResultVO<Void> storageResult = storageFeignClient.deduct(...);
    // 调用账户服务扣减余额
    ResultVO<Void> deduct = accountFeignClient.deduct(...);
    // 创建订单
    OrderDO order = new OrderDO();
    orderMapper.insert(order);
    return order.getId();
}
```

配置说明：
- `@GlobalTransactional`：开启 Seata 全局事务，name 为事务名称
- `rollbackFor`：指定需要回滚的异常类型
- Seata Server 地址在 `application.yml` 中配置（通过 `spring.cloud.alibaba.seata.tx-service-group`）

#### 验证

用"库存足够但余额不足"触发全局事务回滚：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":99999999.00}'
```

然后再次查询：

```bash
curl -sS "http://127.0.0.1:8888/account/api/account/list"
curl -sS "http://127.0.0.1:8888/storage/api/storage/list"
curl -sS "http://127.0.0.1:8888/order/api/order/list"
```

预期：请求失败、订单不新增、余额与库存保持不变。

---

## Sentinel

### Fallback 降级

OpenFeign 整合 Sentinel 实现服务降级，详细参考 [OpenFeign - Fallback 降级配置](#fallback-降级配置)

---

## Nacos

### 服务发现

#### 配置

**1.** Maven 依赖：在 `pom.xml` 中添加 Nacos Discovery 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

**2.** 服务注册配置（在 `application.yml` 中）

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: public
        group: DEFAULT_GROUP
```

配置说明：
- `server-addr`：Nacos 服务器地址
- `namespace`：命名空间（默认 public）
- `group`：服务分组

**3.** 启动类添加 `@EnableDiscoveryClient` 注解（可选，Spring Cloud 自动注册）

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

完成上述步骤的配置之后， 服务启动的时候会自动注册到 Nacos 注册中心.

#### 验证

**查询服务实例列表**

```bash
curl -sS "http://127.0.0.1:8848/nacos/v1/ns/instance/list?serviceName=account-service&groupName=DEFAULT_GROUP"
```

预期：返回 account-service 在 Nacos 注册的所有实例信息，包括 IP、端口、健康状态等。

---

### 动态配置

#### 配置

**1.** Maven 依赖：在 `pom.xml` 中添加 Nacos Config 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

**2.** Controller 配置：

```java
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@RefreshScope
public class OrderController {

    @Value("${order.config.orderNo:default-value}")
    private String orderNo;

    @GetMapping("/config")
    public ResultVO<Map<String, String>> getConfig() {
        return ResultVO.success(Map.of("Nacos Config OrderNo: ", orderNo));
    }
}
```

**3.** Nacos 配置（在 Nacos Console 中添加）：

- **Data ID**：`order-service.yaml`
- **Group**：`DEFAULT_GROUP`
- **配置内容**：
  ```yaml
  order:
    config:
      orderNo: hello-nacos
  ```

> [!NOTE]
> 配置说明：`@RefreshScope` 注解使 `@Value` 配置支持热更新，修改 Nacos 配置后，无需重启服务即可生效

#### 验证

**查询当前配置值**

```bash
curl -sS "http://127.0.0.1:8888/order/api/order/config"
```

预期返回：
```json
{"code":200,"message":"success","data":{"Nacos Config OrderNo: ":"hello-nacos"}}
```

**验证热更新**

将 Nacos 中的 `order.config.orderNo` 修改为 `hello-updated`，无需重启 order-service，再次调用：

```bash
curl -sS "http://127.0.0.1:8888/order/api/order/config"
```

预期返回：
```json
{"code":200,"message":"success","data":{"Nacos Config OrderNo: ":"hello-updated"}}
```

配置变更自动生效，无需重启服务。

---

## OpenFeign

### Feign 客户端创建

**配置**

**1.** Maven 依赖：在 `pom.xml` 中添加 OpenFeign 依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**2.** 启用 OpenFeign：在启动类添加 `@EnableFeignClients` 注解

```java
@SpringBootApplication
@EnableFeignClients
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

**3.** 定义 FeignClient 接口

```java
@FeignClient(
    name = "account-service",
    path = "/api/account"
)
public interface AccountFeignClient {

    @PostMapping("/deduct")
    ResultVO<Void> deduct(@RequestBody DeductAccountRequest request);
}
```

配置说明：
- `name`：目标服务名称（需与服务注册中心一致）
- `path`：请求路径前缀

**验证**

通过订单创建请求验证 Feign 客户端正常调用下游服务：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":2,"money":100.00}'
```

预期：返回成功，订单/库存/余额数据正确变化。

---

#### 均衡负载

> [!NOTE]
> `spring-cloud-starter-loadbalancer` 依赖用于实现客户端负载均衡，默认使用轮询策略。需在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

---

#### Fallback 降级配置

**配置**

**1.** Maven 依赖：添加 Sentinel 依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

**2.** 配置：在 `application.yml` 中开启 Sentinel 支持

```yaml
feign:
  sentinel:
    enabled: true
```

**3.** 创建 FallbackFactory 实现类

```java
@Slf4j
@Component
public class AccountFeignFallbackFactory implements FallbackFactory<AccountFeignClient> {

    @Override
    public AccountFeignClient create(Throwable cause) {
        return request -> fallbackDeduct(request, cause);
    }

    private ResultVO<Void> fallbackDeduct(DeductAccountRequest request, Throwable cause) {
        log.error("account-service fallback triggered, request={}, cause={}", request, cause == null ? "unknown" : cause.getMessage(), cause);
        return ResultVO.fail(503, "account-service degraded, request rejected");
    }
}
```

位置：`com.example.cloud.order.feign.fallback.AccountFeignFallbackFactory`

**4.** 在 FeignClient 接口添加 `fallbackFactory` 属性

```java
@FeignClient(
    name = "account-service",
    path = "/api/account",
    fallbackFactory = AccountFeignFallbackFactory.class
)
public interface AccountFeignClient {

    @PostMapping("/deduct")
    ResultVO<Void> deduct(@RequestBody DeductAccountRequest request);
}
```

> [!NOTE]
> Feign 的 Fallback 需要一个回退执行客户端来实现降级逻辑，本 demo 使用 Sentinel 作为回退执行客户端，也可以使用其他实现（如 Hystrix）。

配置说明：
- `FallbackFactory` 接口用于定义 Feign 客户端的降级逻辑
- 当下游服务不可用时，自动调用 fallback 方法返回降级响应

**验证**

停止 account-service，验证 FallbackFactory 生效：

```bash
# 先确认 account-service 正常运行
curl -sS "http://127.0.0.1:8848/nacos/v1/ns/instance/list?serviceName=account-service&groupName=DEFAULT_GROUP"

# 停止 account-service（IDE 停止或终端 Ctrl+C）

# 发起下单请求
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期：返回降级响应，`message` 包含 `account-service degraded, request rejected`

### 请求拦截器

#### 单个 Feign 客户端

**配置**

**1.** 创建 `RequestInterceptor` 实现类

```java
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String requestId = UUID.randomUUID().toString();
        template.header("X-Request-Id", requestId);
        template.header("X-From-Service", "order-service");
        
        log.info("Feign request intercepted, method={}, url={}, requestId={}", 
                template.httpMethod(), template.url(), requestId);
    }
}
```

**2.** 在 `@FeignClient` 中配置 `configuration`

```java
@FeignClient(
    name = "account-service",
    path = "/api/account",
    fallbackFactory = AccountFeignFallbackFactory.class,
    configuration = FeignRequestInterceptor.class
)
public interface AccountFeignClient {

    @PostMapping("/deduct")
    ResultVO<Void> deduct(@RequestBody DeductAccountRequest request);
}
```

配置说明：
- `RequestInterceptor` 用于在 Feign 发送请求前拦截并修改请求
- 常用场景：添加统一请求头（如认证信息、追踪 ID）、日志记录、请求参数加密等

这里也可以通过配置 `application.yml` 来控制是否启用某些请求拦截器：

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          account-service:
            request-interceptors:
              - com.example.cloud.order.feign.FeignRequestInterceptor
```

**验证**

发送请求，验证请求头传递到下游服务：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

在 account-service 日志中查看是否接收到 `X-Request-Id` 和 `X-From-Service` 请求头。

#### 全局生效

**配置**

通过 `@Component` 注解注册为 Bean，对所有 Feign 客户端生效：

```java
@Slf4j
@Component
public class GlobalFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header("X-TraceId", UUID.randomUUID().toString());
        log.info("[Global] Feign request intercepted, service={}, method={}, url={}, X-TraceId={}",
                template.feignTarget().name(),
                template.request().httpMethod(),
                template.url(),
                template.headers().get("X-TraceId"));
    }
}
```

位置：`com.example.cloud.order.feign.GlobalFeignRequestInterceptor`

> [!NOTE]
> 如果同时使用 `@Component` 全局注册和 `@FeignClient.configuration` 单个客户端配置，**两者都会生效**。

**验证**

发送请求，验证请求头传递到下游服务：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期：在 order-service 日志中看到类似：
```
[Global] Feign request intercepted, service=account-service, method=POST, url=/deduct, X-TraceId=[...]
```

### 响应拦截器

#### 单个 Feign 客户端

**配置**

**1.** 创建 `ResponseInterceptor` 实现类

```java
@Slf4j
public class FeignResponseInterceptor implements ResponseInterceptor {

    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        Response response = invocationContext.response();
        Request request = response.request();
        Object result = chain.next(invocationContext);
        log.info("Feign response intercepted, method={}, url={}, status={}, result={}",
                request.httpMethod(), request.url(), response.status(), result);
        return result;
    }
}
```

位置：`com.example.cloud.order.feign.FeignResponseInterceptor`

**2.** 在 `@FeignClient` 中配置 `configuration`

```java
@FeignClient(
        name = "account-service",
        path = "/api/account",
        fallbackFactory = AccountFeignFallbackFactory.class,
        configuration = {FeignRequestInterceptor.class, FeignResponseInterceptor.class}
)
public interface AccountFeignClient {
    // ...
}
```

这里也可以通过配置 `application.yml` 来控制是否启用某个响应拦截器：

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          account-service:
            response-interceptors: com.example.cloud.order.feign.FeignResponseInterceptor # 仅能添加一个响应拦截器
```

**验证**

发送请求，查看日志输出：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期：在 order-service 日志中看到类似：
```
Feign response intercepted, method=POST, url=/deduct, status=200, result=...
```

#### 全局生效

**配置**

通过 `@Component` 注解注册为 Bean，对所有 Feign 客户端生效：

```java
@Slf4j
@Component
public class GlobalFeignResponseInterceptor implements ResponseInterceptor {

    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        Response response = invocationContext.response();
        Request request = response.request();
        Object result = chain.next(invocationContext);
        log.info("[Global] Feign response intercepted, method={}, url={}, status={}, result={}",
                request.httpMethod(), request.url(), response.status(), result);
        return result;
    }
}
```

位置：`com.example.cloud.order.feign.GlobalFeignResponseInterceptor`

> [!NOTE]
> 如果同时使用 `@Component` 全局注册和 `@FeignClient.configuration` 单个客户端配置，**只有单个客户端配置的拦截器生效，全局拦截器对该客户端不生效**。

**验证**

发送请求，查看日志输出：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期：在 order-service 日志中看到类似：
```
[Global] Feign response intercepted, method=POST, url=/deduct, status=200, result=...
```

### 超时控制

#### 配置

**YAML 配置**

```yaml
feign:
  client:
    config:
      account-service:
        connect-timeout: 3000
        read-timeout: 5000
      order-service:
        connect-timeout: 3000
        read-timeout: 5000
      storage-service:
        connect-timeout: 3000
        read-timeout: 5000
```

配置说明：
- `connect-timeout`：连接超时时间（毫秒），建立 TCP 连接的时间
- `read-timeout`：读取超时时间（毫秒），从发送请求到接收响应的时间

#### 验证

将`account-service`服务暂停，然后调用下面的请求：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期：等待5秒之后，发生读取超时.

### 重试机制

#### 配置

```java
@Configuration
public class FeignRetryConfig {
    @Bean
    public Retryer retryer() {
        // public Default(long period, long maxPeriod, int maxAttempts)
        return new Retryer.Default(100, 1000, 3);
    }
}
```

配置说明：
- `period`: 重试间隔基数（100ms）
- `maxPeriod`: 最大重试间隔（1000ms = 1s）
- `maxAttempts`: 最大尝试次数（含首次），设为 3

位置：`com.example.cloud.order.config.FeignRetryConfig`

#### 验证

停止 account-service，发起请求，然后在快速启动服务，验证重试机制：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

在 order-service 日志中查看重试日志，确认重试次数符合配置。

## Dubbo

Dubbo 是阿里巴巴开源的高性能 RPC 框架，与 Spring Cloud 生态无缝集成。本示例在保留 Feign 的同时增加 Dubbo 作为并行学习路径。

| 特性 | Feign (HTTP) | Dubbo (RPC) |
|------|--------------|-------------|
| 通信协议 | HTTP/1.1 | TCP/Hessian |
| 序列化 | JSON | Hessian |
| 性能 | 较低 | 较高 |
| 适用场景 | HTTP API | 内部服务调用 |

> [!note]
> RPC 是一套远程调用解决方案规范，并非具体协议

---

### 提供者声明

**配置**

在服务中添加依赖：

```xml
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <version>[last-version]</version>
</dependency>
<!-- 使用nacos作为注册中心时导入 -->
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-nacos-spring-boot-starter</artifactId>
</dependency>
```

**2.** 服务配置：在各服务的 `application.yml` 中配置

```yaml
dubbo:
  application:
    name: account-service
  protocol:
    port: 20881
  registry:
    address: nacos://127.0.0.1:8848 // 使用nacos作为注册中心
```



**内置协议**

| 协议 | 配置值 | 说明 | 默认端口 |
|------|-------|------|----------|
| **Dubbo** | `dubbo` | 默认协议，基于 TCP 的私有协议 | 最高性能 |
| **Triple** | `tri` 或 `triple` | 基于 HTTP/1、HTTP/2，兼容 gRPC | 网关友好，支持流 |
| **HTTP** | `http` | 基于 HTTP 的私有协议 | Spring HTTP |
| **REST** | `rest` | REST 风格 HTTP 服务 | 标准 REST API |

配置说明：
- `dubbo` 为默认协议，可省略不配
- `tri` 需显式配置 `name: tri`

**定义公共API**

在 `alibaba-common` 中定义接口：

```java
package com.example.common.dubbo;

public interface DubboAccountService {
    boolean deduct(DeductAccountRequest request);
}
```

```java
package com.example.common.dubbo;

public interface DubboStorageService {
    boolean deduct(DeductStorageRequest request);
}
```

> [!NOTE]
>
> 服务端和消费端可能处于不同的模块，这个时候需要定义好公共api

**Provider 实现**

```java
@DubboService
@RequiredArgsConstructor
public class DubboStorageServiceImpl implements DubboStorageService {

    private final StorageService storageService;

    @Override
    public boolean deduct(DeductStorageRequest request) {
        storageService.deduct(request);
        return true;
    }
}
```

---

**验证**

通过订单创建请求验证提供者服务已正确注册：

查看 Nacos 控制台，确认服务已注册 Dubbo 协议信息。

---

### 消费者声明

**配置**

配置和消费者配置一致

**Consumer 实现**

```java
@Component
@ConditionalOnProperty(name = "remote.call-mode", havingValue = "dubbo")
public class DubboOrderService {

    @DubboReference // 在需要的地方使用@DubboReference注解
    private DubboAccountService dubboAccountService;

    @DubboReference
    private DubboStorageService dubboStorageService;

    public ResultVO<Void> deductStorage(String commodityCode, Integer count) {
        DeductStorageRequest request = new DeductStorageRequest(commodityCode, count);
        boolean success = dubboStorageService.deduct(request);
        return success ? ResultVO.success() : ResultVO.fail("库存扣减失败");
    }

    public ResultVO<Void> deductAccount(String userId, BigDecimal money) {
        DeductAccountRequest request = new DeductAccountRequest(userId, money);
        boolean success = dubboAccountService.deduct(request);
        return success ? ResultVO.success() : ResultVO.fail("账户扣减失败");
    }
}
```

**验证**

通过订单创建请求验证消费者调用：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期：返回成功，订单/库存/余额数据正确变化。

查看 Nacos 控制台，确认消费者已连接 Dubbo 服务。

---

### 拦截器

Dubbo 提供了 Filter 机制，允许在请求、响应、异常等阶段进行拦截和处理。本示例实现了三个核心 Filter：

**声明 Filter**

Filter 需要实现 `org.apache.dubbo.rpc.Filter` 接口。使用 `@Activate` 注解标注 Filter 的作用范围（`group = "consumer"` 表示消费端生效，`group = "provider"` 表示提供端生效）。

- **消费端请求拦截器**：[`DubboConsumerRequestFilter`](alibaba-common/src/main/java/com/example/common/dubbo/filter/DubboConsumerRequestFilter.java) 在消费端（调用者）拦截请求，记录请求日志并添加链路追踪 ID。
- **提供端响应拦截器**：[`DubboProviderResponseFilter`](alibaba-common/src/main/java/com/example/common/dubbo/filter/DubboProviderResponseFilter.java) 在提供端（服务提供者）拦截响应，记录响应日志和耗时。
- **异常处理过滤器**：[`DubboExceptionFilter`](alibaba-common/src/main/java/com/example/common/dubbo/filter/DubboExceptionFilter.java) 在提供端捕获异常，记录详细的异常日志。

> [!TIP]
>
> Dubbo在提供了`org.apache.dubbo.common.constants.CommonConstants`常量，使用内置静态常量可以避免手动书写错误.

**注册Filter**

Filter 通过 Dubbo 的 SPI（Service Provider Interface）机制自动加载，无需在 `application.yml` 中额外配置。在 `META-INF/dubbo/org.apache.dubbo.rpc.Filter` 文件中注册 Filter 的全限定类名：

```properties
# File: alibaba-common/src/main/resources/META-INF/dubbo/org.apache.dubbo.rpc.Filter
dubboConsumerRequestFilter=com.example.common.dubbo.filter.DubboConsumerRequestFilter
dubboProviderResponseFilter=com.example.common.dubbo.filter.DubboProviderResponseFilter
dubboExceptionFilter=com.example.common.dubbo.filter.DubboExceptionFilter
```

Dubbo 启动时会自动扫描 `META-INF/dubbo/` 下的配置文件，加载所有已注册的 Filter，无需手动操作。

> [!TIP]
>
> 上面仅演示了通过Dubbo SPI机制注册Filter，如果使用了Spring框架，也可以直接通过`@Component`注解直接注册

#### **验证**

通过创建订单请求，查看日志输出：

```bash
curl -sS -X POST "http://127.0.0.1:8888/order/api/order/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":"u1001","commodityCode":"C1001","count":1,"money":10.00}'
```

预期查看到以下日志链路：

```
[Dubbo Filter] REQUEST -> DubboStorageService.deduct() | TraceId: xxx | Params: {...}
[Dubbo Filter] REQUEST -> DubboAccountService.deduct() | TraceId: xxx | Params: {...}
[Dubbo Filter] RESPONSE <- DubboStorageService.deduct() | TraceId: xxx | Cost: 25ms | Result: success
[Dubbo Filter] RESPONSE <- DubboAccountService.deduct() | TraceId: xxx | Cost: 30ms | Result: success
```

---

**注意事项**

1. **序列化**：Dubbo 使用 Hessian 序列化，需确保 DTO 实现 `Serializable`
2. **端口冲突**：确保 Dubbo 端口（20881/20882/20880）未被占用
3. **注册中心**：本示例复用 Nacos，也可使用 ZooKeeper
