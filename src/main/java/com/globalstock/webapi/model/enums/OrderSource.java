package com.globalstock.webapi.model.enums;

import java.util.Locale;

/**
 * 采购订单来源。
 */
public enum OrderSource {

    SELF_SERVICE("self_service"),
    ADMIN_GRANT("admin_grant");

    private final String code;

    OrderSource(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OrderSource fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SELF_SERVICE;
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        for (OrderSource source : values()) {
            if (source.code.equals(normalized)) {
                return source;
            }
        }
        throw new IllegalArgumentException("未知订单来源: " + code);
    }
}
