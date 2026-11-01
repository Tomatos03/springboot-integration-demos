package org.demo.jwt.dual.store;

import java.util.Optional;

/**
 * refresh token 的服务端状态存储。
 *
 * <p>demo 使用内存实现；生产环境应替换为 Redis 或数据库，并把 {@code revokeFamily}
 * 做成原子操作，以支持多实例部署与主动登出。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public interface RefreshTokenStore {

    void save(RefreshSession session);

    Optional<RefreshSession> find(String jti);

    /**
     * 原子地把会话标记为已使用，用于保证 refresh token 只能被消费一次。
     *
     * @return true 表示本次调用完成标记；false 表示会话不存在或已被使用
     */
    boolean markUsed(String jti);

    void revokeFamily(String familyId);
}
