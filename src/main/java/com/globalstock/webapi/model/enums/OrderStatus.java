package com.globalstock.webapi.model.enums;

import java.util.Locale;

/**
 * 采购订单状态。
 */
public enum OrderStatus {

    PENDING_REVIEW("pending_review"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CANCELLED("cancelled");

    private final String code;

    OrderStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("订单状态不能为空");
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        for (OrderStatus status : values()) {
            if (status.code.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }
}
