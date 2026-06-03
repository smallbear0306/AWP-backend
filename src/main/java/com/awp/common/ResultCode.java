package com.awp.common;

/**
 * 业务返回码枚举。code 约定：
 * 0       成功
 * 1xxx    通用/参数类错误
 * 2xxx    用户/鉴权类错误
 */
public enum ResultCode {

    SUCCESS(0, "success"),

    PARAM_ERROR(1001, "参数错误"),
    NOT_FOUND(1002, "资源不存在"),
    SYSTEM_ERROR(1003, "系统异常"),

    UNAUTHORIZED(2001, "未登录或登录已过期"),
    USERNAME_EXISTS(2002, "用户名已存在"),
    LOGIN_FAILED(2003, "用户名或密码错误");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
