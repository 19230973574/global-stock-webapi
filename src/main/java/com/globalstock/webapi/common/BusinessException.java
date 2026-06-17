package com.globalstock.webapi.common;

/**
 * 业务异常。
 *
 * <p>职责说明：表示参数错误、权限不足、业务规则不满足等可预期异常。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
public class BusinessException extends RuntimeException {

    /**
     * 创建业务异常。
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
    }
}
