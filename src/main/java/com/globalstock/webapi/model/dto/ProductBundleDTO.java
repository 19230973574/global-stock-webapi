package com.globalstock.webapi.model.dto;

import java.util.List;

public record ProductBundleDTO(
        String id,
        String name,
        String description,
        List<String> productIds,
        Long priceAmount,
        String priceCurrency,
        String priceDisplay,
        Long apiQuotaMonthly,
        boolean popular,
        String status
) {
}
