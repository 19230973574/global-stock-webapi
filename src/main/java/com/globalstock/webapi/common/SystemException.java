package com.globalstock.webapi.common;

/**
 * 系统异常。
 *
 * <p>职责说明：包装数据库、网络、序列化等不可预期系统错误。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
public class SystemException extends RuntimeException {

    /**
     * 创建系统异常。
     *
     * @param message 异常消息
     * @param cause 原始异常
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
