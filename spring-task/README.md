# Spring-Task

## 项目结构

```
src/main/java/org/demo/
├── SpringTaskApplication.java  # 启动类
├── config/
│   └── ThreadPoolConfig.java # 线程池配置
├── task/
│   ├── ScheduledTask.java    # 定时任务（4种场景）
│   └── AsyncTaskService.java # 异步任务（3种场景）
└── controller/
    └── TaskController.java   # REST 接口
```

## 关键配置

**启动类注解**
```java
@EnableScheduling  // 定时任务
@EnableAsync       // 异步任务
```

**线程池参数**
| 参数 | 值 | 说明 |
|------|-----|------|
| corePoolSize | 5 | 核心线程数 |
| maxPoolSize | 10 | 最大线程数 |
| queueCapacity | 100 | 队列容量 |
| threadNamePrefix | async-task- | 线程名前缀 |
| rejectedExecutionHandler | CallerRunsPolicy | 拒绝策略 |

## 定时任务 (@Scheduled)

| 方式 | 配置 | 说明 |
|------|------|------|
| fixedRate | `5000` | 每 5 秒执行（不等上次完成） |
| fixedDelay | `10000` | 上次完成后等 10 秒再执行 |
| cron | `0 0 2 * * ?` | 每天凌晨 2 点 |
| initialDelay + fixedRate | `10000, 5000` | 启动后等 10 秒，再每 5 秒执行 |

**Cron 表达式格式**：秒 分 时 日 月 周 [年]

常用示例：
- `0 0/5 * * * ?` - 每 5 分钟
- `0 0 12 * * ?` - 每天中午 12 点
- `0 0/30 9-17 * * ?` - 朝九晚五每半小时

## 异步任务 (@Async)

| 场景 | 方法 | 返回值 |
|------|------|--------|
| 发送邮件 | `sendEmail(to)` | void |
| 数据处理 | `processData()` | CompletableFuture |
| 批量导入 | `batchImport(fileName)` | void |

## 测试方式

**启动后定时任务自动执行**

**异步任务接口**
```bash
# 发送邮件
curl -X POST http://localhost:8080/task/send-email \
  -H "Content-Type: application/json" \
  -d '{"to":"test@example.com"}'

# 数据处理
curl -X POST http://localhost:8080/task/process-data

# 批量导入
curl -X POST http://localhost:8080/task/batch-import \
  -H "Content-Type: application/json" \
  -d '{"fileName":"orders.xlsx"}'

# 批量触发多个任务
curl -X POST http://localhost:8080/task/batch-trigger
```

## 注意事项

1. **@Async 限制**
   - 方法必须是 public
   - 不能在同一个类中调用（AOP 代理限制）
   - 需要 @EnableAsync 注解

2. **线程池调优**
   - CPU 密集型：corePoolSize = CPU 核数 + 1
   - IO 密集型：corePoolSize = CPU 核数 * 2

3. **fixedRate vs fixedDelay**
   - fixedRate: 不管上次是否完成，到时间就执行
   - fixedDelay: 等上次完成后，再等指定时间
