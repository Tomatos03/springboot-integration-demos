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

## 交换机类型详解

RabbitMQ 提供了四种交换机类型，本Demo演示其中最常用的三种：Direct（直连）、Fanout（广播）、Topic（主题）交换机如何配置和使用

### 交换机特点对比

| 交换机类型 | 路由方式 | 使用场景 |
|----------|---------|---------|
| **Direct** | 精确匹配路由键 | 点对点消息传递，需要精确路由 |
| **Fanout** | 忽略路由键，广播到所有绑定队列 | 广播通知，消息需要发送给所有订阅者 |
| **Topic** | 通配符模式匹配路由键 | 复杂的消息分发规则，支持模糊匹配 |



### 配置队列和交换机

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

### Topic Exchange 通配符规则

- `*` (星号)：匹配**一个**单词
- `#` (井号)：匹配**零个或多个**单词

**示例说明：**
- `china.#` 可以匹配：`china.news`、`china.sports`、`china.tech.ai` 等
- `*.news` 可以匹配：`china.news`、`usa.news`、`uk.news` 等
- `china.*.sports` 可以匹配：`china.beijing.sports`、`china.shanghai.sports` 等

## 高级特性

### 延迟消息

在**RabbitMq**中并没有原生支持延迟消息功能，需要额外安装 [RabbitMQ 延迟消息插件](https://github.com/rabbitmq/rabbitmq-delayed-message-exchange)。

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

    rabbitTemplate.convertAndSend(exchangeName, routeKey, "delayed message", message -> {
        message.getMessageProperties().setDelayLong(10000L);  // 延迟10秒
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
      retry:
        enabled: true                           # 开启重试
        max-attempts: 3                         # 最大重试次数
        initial-interval: 1000                  # 初始重试间隔(ms)
        multiplier: 2.0                         # 重试间隔倍数
        max-interval: 10000                     # 最大重试间隔(ms)
```

#### 消费者配置

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        acknowledge-mode: auto                  # 自动确认模式
        prefetch: 1                            # 每次预取消息数量
        retry:
          enabled: true                         # 开启重试
          max-attempts: 3                       # 最大重试次数
          initial-interval: 1000                # 初始重试间隔(ms)
          multiplier: 2.0                       # 重试间隔倍数
          max-interval: 10000                   # 最大重试间隔(ms)
```

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
