package com.globalstock.webapi.model.dto;

/**
 * 审计日志 DTO。
 */
public record AuditLogDTO(
        String id,
        String action,
        String operatorEmail,
        String targetEmail,
        String resourceType,
        String resourceId,
        String detail,
        String before,
        String after,
        Long createdAt
) {
}
