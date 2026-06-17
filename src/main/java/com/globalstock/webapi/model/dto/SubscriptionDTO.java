package com.globalstock.webapi.model.dto;

public record SubscriptionDTO(
        String id,
        String accountEmail,
        String productId,
        String productName,
        String status,
        Long startsAt,
        Long expiresAt,
        String orderId,
        String grantedBy
) {
}
