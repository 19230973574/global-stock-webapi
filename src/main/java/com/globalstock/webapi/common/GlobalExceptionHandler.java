package com.globalstock.webapi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>职责说明：将 Service 层异常转换为统一 API 响应。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        return ApiResponse.failure("BUSINESS_ERROR", exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public org.springframework.http.ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
            UnauthorizedException exception) {
        String code = exception.getHttpStatus() == 403 ? "FORBIDDEN" : "UNAUTHORIZED";
        return org.springframework.http.ResponseEntity
                .status(exception.getHttpStatus())
                .body(ApiResponse.failure(code, exception.getMessage()));
    }

    /**
     * 处理系统异常。
     *
     * @param exception 系统异常
     * @return 失败响应
     */
    @ExceptionHandler(SystemException.class)
    public ApiResponse<Void> handleSystemException(SystemException exception) {
        log.error("system exception", exception);
        return ApiResponse.failure("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
