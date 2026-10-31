package org.demo.context;

/**
 * 当前请求的用户上下文。
 *
 * <p>使用 ThreadLocal 保存当前线程的用户信息。必须在请求结束时调用 {@link #clear()}，
 * 否则线程复用可能导致用户信息串到下一个请求。</p>
 *
 * <p>仅适用于同步的线程-请求模型；在 {@code @Async}、线程池或 WebFlux 场景中需要额外传播上下文。</p>
 *
 * @author : Tomatos
 * @date : 2026/3/31
 */
public final class UserContext {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(UserInfo userInfo) {
        HOLDER.set(userInfo);
    }

    /**
     * 获取当前请求的用户信息。
     *
     * @throws IllegalStateException 当前线程没有用户上下文
     */
    public static UserInfo get() {
        UserInfo userInfo = HOLDER.get();
        if (userInfo == null) {
            throw new IllegalStateException("当前请求没有用户上下文");
        }
        return userInfo;
    }

    /**
     * 获取当前请求的用户信息，没有时返回 null。
     */
    public static UserInfo getOrNull() {
        return HOLDER.get();
    }

    public static Integer getUserId() {
        return get().userId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
