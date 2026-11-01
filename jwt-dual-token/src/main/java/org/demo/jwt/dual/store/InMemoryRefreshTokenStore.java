package org.demo.jwt.dual.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 基于 {@link ConcurrentHashMap} 的 refresh token 存储。
 *
 * <p>进程重启即清空，且只在单实例内有效，仅用于演示轮换与重放检测。
 * 过期的会话不会自动清理，生产实现应交给 Redis 的 TTL。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
@Component
public class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private final Map<String, RefreshSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void save(RefreshSession session) {
        sessions.put(session.jti(), session);
    }

    @Override
    public Optional<RefreshSession> find(String jti) {
        if (jti == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(jti));
    }

    @Override
    public boolean markUsed(String jti) {
        AtomicBoolean marked = new AtomicBoolean(false);
        sessions.computeIfPresent(jti, (key, session) -> {
            if (session.used()) {
                return session;
            }
            marked.set(true);
            return session.markUsed();
        });
        return marked.get();
    }

    @Override
    public void revokeFamily(String familyId) {
        sessions.values().removeIf(session -> session.familyId().equals(familyId));
    }
}
