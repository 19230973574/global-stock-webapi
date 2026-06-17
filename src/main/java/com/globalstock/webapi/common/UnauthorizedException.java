package com.globalstock.webapi.common;

/**
 * 未授权或权限不足异常。
 */
public class UnauthorizedException extends RuntimeException {

    private final int httpStatus;

    public UnauthorizedException(String message) {
        this(message, 401);
    }

    public UnauthorizedException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
