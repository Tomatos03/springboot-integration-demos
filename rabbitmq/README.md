# SpringBoot 整合 RabbitMQ

本项目演示了如何在 SpringBoot 中集成 RabbitMQ，包含生产者(Producer)和消费者(Consumer)两个模块，展示了 RabbitMQ 的三种常见交换机类型的配置和使用。

## 快速开始

### 1. 安装 RabbitMQ

确保本地已安装并启动 RabbitMQ 服务，默认端口为 5672。

### 2. 配置连接信息

在 `application.yml` 中配置 RabbitMQ 连接信息：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: your_username
    password: your_password
    virtual-host: "your_vhost"
```

### 3. 启动应用

分别启动 Producer 和 Consumer 应用即可开始测试。

## 基本使用

这里演示如何将交换机绑定到队列, 怎么发送消息到交换机, 以及如何监听队列接收消息。

### 绑定交换机与队列

在RabbitMQ中必须生产者投递消息不能够直接投递到队列, 需要投递到交换机, 然后由交换机传递消息到队列

> [!NOTE]
>
> RabbitMQ中一个名为""的默认交换机默认绑定所有已经存在的队列, routingKey 为队列名. 使用这个默认交换机能够间接实现直接投递消息到队列的效果

#### 配置类方式

```java
@Configuration
public class RabbitMQConfig {

    // ==================== Direct Exchange 配置 ====================

    /*
     * durable = true
     * 队列配置后：RabbitMQ 服务重启后，队列/交换机依然存在，消息不会丢失（前提是消息本身也设置为持久化）。
     * 交换机配置后: RabbitMQ 服务重启后，队列/交换机会被删除，消息也会丢失。
     *
     * autoDelete = true
     * 队列配置后: 最后一个消费者断开连接后，队列自动删除
     * 交换机配置后:没有队列绑定时，交换机自动删除
     */
    @Bean
    public DirectExchange directExchange() {
        // 交换机和队列还提供了Builder模式进行配置
        return new DirectExchange(
            "rabbitmq.direct",    // 交换机名称
            true,                 // 是否持久化
            false                 // 是否自动删除
        );
    }

    @Bean
    public Queue directQueue() {
        return new Queue(
            "direct.queue",      // 队列名称
            true                 // 是否持久化
        );
    }

    @Bean
    public Binding bindingDirect1(Queue directQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(directQueue)    // 绑定队列
                           .to(directExchange)       // 到交换机
                           .with("blue");            // 使用路由键 "blue"
    }

    // ==================== Fanout Exchange 配置 ====================

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(
            "rabbitmq.fanout",    // 交换机名称
            true,                 // 是否持久化
            false                 // 是否自动删除
        );
    }

    @Bean
    public Queue fanoutQueue1() {
        return new Queue("fanout.queue", true);
    }

    @Bean
    public Binding bindingFanout1(Queue fanoutQueue1, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(fanoutQueue1)    // 绑定队列
                             .to(fanoutExchange);   // 到广播交换机（无需路由键, 如果配置了也不影响）
    }

    // ==================== Topic Exchange 配置 ====================

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(
            "rabbitmq.topic",     // 交换机名称
            true,                 // 是否持久化
            false                 // 是否自动删除
        );
    }

    @Bean
    public Queue topicQueue1() {
        return new Queue("topic.queue1", true);
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue("topic.queue2", true);
    }

    @Bean
    public Binding bindingTopic1(Queue topicQueue1, TopicExchange topicExchange) {
        return BindingBuilder.bind(topicQueue1)
                           .to(topicExchange)
                           .with("china.#");         // # 匹配零个或多个单词
    }

    @Bean
    public Binding bindingTopic2(Queue topicQueue2, TopicExchange topicExchange) {
        return BindingBuilder.bind(topicQueue2)
                           .to(topicExchange)
                           .with("*.news");          // * 匹配一个单词
    }
}
```

#### 注解配置方式

这种方式不仅仅绑定交换机到队列, 还设置了监听的队列

```java
@Component
public class MessageConsumer {

    // ==================== Direct Exchange 消费者 ====================

    // 监听的队列和交换机不存在时, 按照声明的规则自动创建
    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(
                value = "direct.queue",          // 队列名称
                durable = "true"                  // 是否持久化
            ),
            exchange = @Exchange(
                value = "rabbitmq.direct",        // 交换机名称
                type = ExchangeTypes.DIRECT,      // 交换机类型
                durable = "true"                  // 是否持久化
            ),
            key = "blue"                          // 路由键
        )
    )
    public void receiveDirectMessage1(String message) {
        System.out.println("Direct Queue received: " + message);
    }

    // ==================== Fanout Exchange 消费者 ====================

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(
                value = "fanout.queue",
                durable = "true"
            ),
            exchange = @Exchange(
                value = "rabbitmq.fanout",
                type = ExchangeTypes.FANOUT       // 广播类型交换机
            )
            // 广播交换机无需指定路由键
        )
    )
    public void receiveFanoutMessage1(String message) {
        System.out.println("Fanout Queue1 received: " + message);
    }
    // ==================== Topic Exchange 消费者 ====================

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "topic.queue1", durable = "true"),
            exchange = @Exchange(
                value = "rabbitmq.topic",
                type = ExchangeTypes.TOPIC        // 主题类型交换机
            ),
            key = "china.#"                       // 匹配 china.* 模式的路由键
        )
    )
    public void receiveTopicMessage1(String message) {
        System.out.println("Topic Queue1 received: " + message);
    }

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "topic.queue2", durable = "true"),
            exchange = @Exchange(value = "rabbitmq.topic", type = ExchangeTypes.TOPIC),
            key = "*.news"                        // 匹配 *.news 模式的路由键
        )
    )
    public void receiveTopicMessage2(String message) {
        System.out.println("Topic Queue2 received: " + message);
    }
}
```

### 消息发送

在测试类中演示如何向三种不同的交换机发送消息：

```java
@SpringBootTest
public class RabbitProducerTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // ==================== Direct Exchange 发送消息 ====================

    @Test
    public void testSendMessageToDirectExchange() {
        String exchangeName = "rabbitmq.direct";
        String message = "Hello Direct Exchange!";

        // 发送给 blue 路由键，只有 direct.queue1 会收到
        rabbitTemplate.convertAndSend(
            exchangeName,                         // 交换机名称
            "blue",                              // 路由键（必须精确匹配）
            message                              // 消息内容
        );

        // 发送给 red 路由键，只有 direct.queue2 会收到
        rabbitTemplate.convertAndSend(exchangeName, "red", "Red message");
    }

    // ==================== Fanout Exchange 发送消息 ====================

    @Test
    public void testSendMessageToFanoutExchange() {
        String exchangeName = "rabbitmq.fanout";
        String message = "Hello Fanout Exchange!";

        rabbitTemplate.convertAndSend(
            exchangeName,                         // 交换机名称
            "",                                  // 路由键（广播模式可为空）
            message                              // 消息内容
        );
        // 消息会发送到所有绑定该交换机的队列
    }

    // ==================== Topic Exchange 发送消息 ====================

    @Test
    public void testSendMessageToTopicExchange() {
        String exchangeName = "rabbitmq.topic";

        // 发送到 china.news，匹配 "china.#" 和 "*.news" 两个模式
        rabbitTemplate.convertAndSend(
            exchangeName,
            "china.news",                        // 路由键
            "China news message"
        );

        // 发送到 china.sports，只匹配 "china.#" 模式
        rabbitTemplate.convertAndSend(exchangeName, "china.sports", "China sports message");

        // 发送到 usa.news，只匹配 "*.news" 模式
        rabbitTemplate.convertAndSend(exchangeName, "usa.news", "USA news message");
    }
}
```
### 消息监听

在消费者模块中，可以通过 `@RabbitListener` 注解实现对队列的消息监听。当有新消息到达队列时，监听方法会自动被调用，处理收到的消息。

```java
@Component
public class RabbitMQListener {

    // 监听 Direct 队列
    @RabbitListener(queues = "direct.queue")
    public void listenDirectQueue(String message) {
        System.out.println("收到 Direct 队列消息: " + message);
    }

    // 监听 Fanout 队列
    @RabbitListener(queues = "fanout.queue")
    public void listenFanoutQueue(String message) {
        System.out.println("收到 Fanout 队列消息: " + message);
    }

    // 监听 Topic 队列1
    @RabbitListener(queues = "topic.queue1")
    public void listenTopicQueue1(String message) {
        System.out.println("收到 Topic 队列1消息: " + message);
    }

    // 监听 Topic 队列2
    @RabbitListener(queues = "topic.queue2")
    public void listenTopicQueue2(String message) {
        System.out.println("收到 Topic 队列2消息: " + message);
    }
}
```


## 交换机类型详解

RabbitMQ 提供了四种交换机类型，本Demo演示其中最常用的三种：Direct（直连）、Fanout（广播）、Topic（主题）交换机如何配置和使用

### 交换机特点对比

| 交换机类型 | 路由方式 | 使用场景 |
|----------|---------|---------|
| **Direct** | 精确匹配路由键 | 点对点消息传递，需要精确路由 |
| **Fanout** | 忽略路由键，广播到所有绑定队列 | 广播通知，消息需要发送给所有订阅者 |
| **Topic** | 通配符模式匹配路由键 | 复杂的消息分发规则，支持模糊匹配 |

### Topic Exchange 通配符规则

- `*` (星号)：匹配**一个**单词
- `#` (井号)：匹配**零个或多个**单词

**示例说明：**
- `china.#` 可以匹配：`china.news`、`china.sports`、`china.tech.ai` 等
- `*.news` 可以匹配：`china.news`、`usa.news`、`uk.news` 等
- `china.*.sports` 可以匹配：`china.beijing.sports`、`china.shanghai.sports` 等

## 高级特性

### 延迟消息

**延迟消息**: 交换机在指定时间后投递消息到消息队列中, 在**RabbitMq**中并没有原生支持延迟消息功能，需要额外安装 [RabbitMQ 延迟消息插件](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)。

#### 配置延迟交换机

```java
@RabbitListener(
    bindings = @QueueBinding(
        value = @Queue(value = "delayed.queue", durable = "true"),
        exchange = @Exchange(
            value = "rabbitmq.delayed",
            delayed = "true"                      // 启用延迟功能
        ),
        key = "delayed.key"
    )
)
public void receiveDelayedMessage(String message) {
    log.info("Received delayed message: {}", message);
}
```

#### 发送延迟消息

```java
@Test
public void testDelaySendMessage() {
    String exchangeName = "rabbitmq.delayed";
    String routeKey = "delayed.key";

    // convertAndSend(exchangeName, routeKey, message, messagePostProcessor)
    rabbitTemplate.convertAndSend(exchangeName, routeKey, "delayed message", message -> {
        message.getMessageProperties().setDelay(10000);  // 延迟10秒(单位毫秒)
        return message;
    });
}
```

### 消息可靠性保证

#### 生产者配置

```yaml
spring:
  rabbitmq:
    publisher-confirm-type: correlated          # 开启发布确认（异步回调）
	publisher-returns: true                     # 开启消息返回机制
    template:
      mandatory: true
      retry:
        enabled: true                           # 开启重试
        max-attempts: 3                         # 最大重试次数
        initial-interval: 1000                  # 初始重试间隔(ms)
        multiplier: 2.0                         # 重试间隔倍数
        max-interval: 10000                     # 最大重试间隔(ms)
```

##### publisher-returns

当属性值设置为`true`时, 如果消息投递到交换机成功, 但是没有匹配到任何队列, 则会触发**消息返回回调**。

##### publisher-confirm-type

当前属性设置为非`none`值时, 可为消息配置`confirm`回调, `confirm`回调**在消息成功到达 RabbitMQ Broker 后才会触发(无论是 ack 还是 nack)**

**ack: **消息成功交由指定的交换机并持久化(如果开启了持久化)

**nack: ** 

+ 消息指定的交换机不存在
+ 交换机配置错误或权限不足
+ Broker 内部错误(如磁盘满、内存溢出)
+ 消息被强制拒绝

> [!NOTE]
>
> 消息到达RabbiMQ Broker不意味着一定有被路由

| 可选值         | 说明                                                         |
|---------------|--------------------------------------------------------------|
| **none**      | 不启用发布确认机制，消息发送后不进行确认回调。                |
| **correlated**| 启用异步发布确认，发送消息后通过回调接口异步接收确认结果。    |
| **simple**    | 启用同步发布确认，发送消息后会阻塞等待RabbitMQ返回确认结果。  |

###### 单条消息设置confirm回调

```java
public void addCouponToQueue(Long voucherId) {
    VoucherOrder voucherOrder = createOrder(voucherId);
    String orderJson = JSONUtil.toJsonStr(voucherOrder);

    // 针对单条消息设置回调
    CorrelationData correlationData = new CorrelationData(UUID.fastUUID().toString(true));
    correlationData.getFuture().addCallback(
            this::handleAddVoucherToQueueSuccess, 
            this::handleAddVoucherToQueueFailure
    );
    rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTE_KEY, orderJson, correlationData);
}

private VoucherOrder createOrder(Long voucherId) {
    return VoucherOrder.builder()
                       .userId(UserHolder.getUser().getId())
                       .voucherId(voucherId)
                       .build();
}

// 消息成功无法到达RabbitMQ Broker时触发的回调
private void handleAddVoucherToQueueFailure(Throwable e) {
    log.info("消息投递失败");
}

// 消息成功到达RabbitMQ Broker时触发的回调
private void handleAddVoucherToQueueSuccess(CorrelationData.Confirm ok) {
    log.info("消息投递成功");
}
```

#### 消费者配置

```yaml
spring:
  rabbitmq:
    listener:
      simple:
      	# 消息没有被成功消息(抛异常或nack)时, 使用默认的拒绝策略重入队
      	# 仅在没有设置retry时生效, 默认值为true(当抛出异常时会无限重试)
        default-requeue-rejected: false 		
        acknowledge-mode: auto                  # 消息确认模式（auto/manual/none）
        prefetch: 1								# 每次预取消息数量
        # Spring AMQP 提供的生产者重试机制
        retry:
          enabled: true                         # 开启重试
          max-attempts: 3                       # 最大重试次数
          initial-interval: 1000                # 初始重试间隔(ms)
          multiplier: 2.0                       # 重试间隔倍数
          max-interval: 10000                   # 最大重试间隔(ms)
```
##### acknowledge-mode

`acknowledge-mode` 用于控制消息消费后的确认机制，常用参数如下：

| 参数值      | 说明                                                         | 适用场景           |
|-------------|--------------------------------------------------------------|--------------------|
| **auto**    | 自动确认。消息被监听方法成功消费后自动确认，无需手动处理。    | 默认推荐，简单场景 |
| **manual**  | 手动确认。需要在代码中显式调用 `channel.basicAck` 或 `channel.basicNack` 方法进行消息确认或拒绝。 | 业务复杂、需精细控制消息处理结果时 |
| **none**    | 不确认。RabbitMQ 不会等待任何确认，消息一旦投递即认为已消费。 | 性能优先但有丢失风险 |

- **auto**：Spring AMQP 默认模式，监听方法无异常即自动确认消息，异常时自动拒绝并可重试。
- **manual**：需在监听方法参数中加入 `Channel` 和 `Message`，通过代码手动确认或拒绝消息，适合需要幂等性、事务等复杂业务场景。
- **none**：不推荐生产环境使用，消息可能丢失，适合对可靠性要求极低的场景。

> [!NOTE]
> 在SpringBoot 之中，默认使用 `auto` 模式，当监听方法执行成功时，消息会被自动确认；如果监听方法抛出异常，消息会被拒绝并根据配置进行重试或丢弃。

##### retry

默认值为`false`, 设置为`true`时, 消费者处理消息出现异常时, 不断的进行重试, 直到重试次数达到`max-attempts`. 到达最大重试次数后创建一个名为`error.excahnge`的交换机, 并通过路由健`error.msg`绑定到队列`error.queue`. 然后将错误信息投递到`error.exchange`交换机, 路由到`error.queue`

##### default-requeue-rejected

默认值为`true`, 当消费者处理消息出现异常时, 是否重新入队. 如果异常一直存在, 在其值设置为`true`时, 会无限重试造成cpu空转

> [!note]
>
> 仅在retry的enable值为`false`时生效

## 注意事项

1. **性能考虑**：发布确认机制会影响性能，非必要情况下不建议开启
2. **延迟消息**：需要安装对应的 RabbitMQ 插件才能使用
3. **消息持久化**：生产环境建议开启队列和消息的持久化
4. **异常处理**：实现适当的异常处理和重试机制
5. **轮询分发**：存在多个消费者的时候, 每条消息只会被一个消费者消费, 消费分发采用轮询的方式
6. **资源管理**：及时释放连接和通道资源

## 参考资料

- [RabbitMQ 官方文档](https://www.rabbitmq.com/documentation.html)
- [Spring AMQP 文档](https://docs.spring.io/spring-amqp/reference/html/)
- [RabbitMQ 延迟消息插件](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)
