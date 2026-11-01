package org.demo.jwt.dual.context;

/**
 * 基于 ThreadLocal 保存当前请求的用户信息，由拦截器写入、请求结束时清理。
 *
 * <p>仅适用于同步 MVC 请求；异步线程或响应式编程需要额外做上下文传播。</p>
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public final class UserContext {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(UserInfo userInfo) {
        HOLDER.set(userInfo);
    }

    public static UserInfo get() {
        UserInfo userInfo = HOLDER.get();
        if (userInfo == null) {
            throw new IllegalStateException("当前请求没有用户上下文");
        }
        return userInfo;
    }

    public static UserInfo getOrNull() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
