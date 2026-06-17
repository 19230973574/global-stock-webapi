package com.globalstock.webapi.model.dto;

public record ApiUsageDTO(
        String usageMonth,
        long callCount,
        Long quotaMonthly,
        boolean unlimited
) {
}
