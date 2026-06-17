package com.globalstock.webapi.common;

/**
 * API 统一响应体。
 *
 * <p>职责说明：为前端提供稳定的成功/失败响应结构。</p>
 *
 * @param code 响应码
 * @param message 响应消息
 * @param data 响应数据
 * @param <T> 数据类型
 * @author Global Stock Team
 * @since 0.0.1
 */
public record ApiResponse<T>(String code, String message, T data) {

    /**
     * 构造成功响应。
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("0", "success", data);
    }

    /**
     * 构造失败响应。
     *
     * @param code 响应码
     * @param message 响应消息
     * @return 失败响应
     */
    public static ApiResponse<Void> failure(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
