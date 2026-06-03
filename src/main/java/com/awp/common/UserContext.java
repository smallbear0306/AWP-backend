package com.awp.common;

/**
 * 当前登录用户上下文：由鉴权拦截器写入，Controller/Service 读取。
 * 基于 ThreadLocal，请求结束后必须清理，避免线程池复用导致串号。
 */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
