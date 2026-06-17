package com.globalstock.webapi.model.dto;

public record UsageSummaryDTO(
        String monthKey,
        long usedCalls,
        Long quotaLimit,
        boolean unlimited
) {
}
