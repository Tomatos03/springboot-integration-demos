package com.demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 *
 * <p>相比手动实现的分布式锁，Redisson 提供了以下优势：
 * <ul>
 *   <li>可重入锁（Reentrant Lock）：同一线程可多次获取同一把锁</li>
 *   <li>自动续期（Watch Dog）：锁到期前自动续期，防止业务未执行完锁就过期</li>
 *   <li>支持公平锁（Fair Lock）：按请求顺序获取锁</li>
 *   <li>支持读写锁（ReadWrite Lock）：读写分离，提高并发性能</li>
 *   <li>支持联锁（MultiLock）：同时获取多把锁</li>
 *   <li>支持红锁（RedLock）：解决主从切换丢锁问题</li>
 *   <li>支持信号量（Semaphore）和闭锁（CountDownLatch）</li>
 * </ul>
 *
 * @author Tomatos
 * @date 2026/4/2
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(5);
        return Redisson.create(config);
    }
}
