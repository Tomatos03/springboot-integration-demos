package org.demo.jwt.dual.store;

import java.time.Instant;

/**
 * 服务端保存的 refresh token 会话状态。
 *
 * <p>同一个 familyId 代表同一条登录会话：第一次登录产生一个 family，
 * 之后每次轮换出的 refresh token 都继承该 family。检测到重放时按 family 整体吊销。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public record RefreshSession(String jti, String familyId, int userId, String username,
                             Instant expiresAt, boolean used) {

    public RefreshSession markUsed() {
        return new RefreshSession(jti, familyId, userId, username, expiresAt, true);
    }
}
