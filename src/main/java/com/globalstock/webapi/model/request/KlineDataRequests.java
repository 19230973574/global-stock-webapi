package com.globalstock.webapi.model.request;

import java.util.List;

public final class KlineDataRequests {

    private KlineDataRequests() {
    }

    public record CreateFetchTaskRequest(
            String market,
            String period,
            List<String> codes,
            String startDate,
            String endDate
    ) {
    }

    public record DeleteKlineRequest(
            String market,
            String period,
            List<String> codes,
            String startDate,
            String endDate,
            List<String> dates,
            Boolean confirm
    ) {
    }

    public record RebuildKlineRequest(
            String market,
            String period,
            List<String> codes,
            Boolean allCodes,
            String startDate,
            String endDate,
            Boolean confirm,
            String confirmPhrase
    ) {
    }
}
