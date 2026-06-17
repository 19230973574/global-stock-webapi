package com.globalstock.webapi.model.dto;

public record TokenUsageDTO(
        String tokenId,
        String usageMonth,
        long callCount
) {
}
