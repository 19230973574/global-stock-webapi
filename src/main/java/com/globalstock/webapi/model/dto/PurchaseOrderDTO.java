package com.globalstock.webapi.model.dto;

/**
 * 采购订单 DTO。
 */
public record PurchaseOrderDTO(
        String id,
        String orderNo,
        String accountEmail,
        String permission,
        String status,
        String source,
        Long submittedAt,
        Long reviewedAt,
        String reviewedBy,
        String rejectReason
) {
}
