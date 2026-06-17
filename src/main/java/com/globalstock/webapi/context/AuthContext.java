package com.globalstock.webapi.context;

/**
 * 当前请求登录上下文（ThreadLocal）。
 */
public final class AuthContext {

    private static final ThreadLocal<SessionInfo> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public record SessionInfo(String email, String role, String token) {
    }

    public static void set(SessionInfo session) {
        CURRENT.set(session);
    }

    public static SessionInfo get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
