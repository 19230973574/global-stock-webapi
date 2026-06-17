package com.globalstock.webapi.model.dto;

import java.util.List;

public record DataProductDTO(
        String id,
        String name,
        String group,
        String description,
        Long priceAmount,
        String priceCurrency,
        String priceDisplay,
        Integer periodMonths,
        List<String> requires,
        Long apiQuotaMonthly,
        String status
) {
}
