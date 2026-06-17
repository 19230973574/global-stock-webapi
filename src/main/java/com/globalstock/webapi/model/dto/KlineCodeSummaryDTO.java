package com.globalstock.webapi.model.dto;

import org.springframework.data.annotation.Id;

public class KlineCodeSummaryDTO {

    @Id
    private String code;
    private String latestDate;
    private Double close;
    private long barCount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(String latestDate) {
        this.latestDate = latestDate;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public long getBarCount() {
        return barCount;
    }

    public void setBarCount(long barCount) {
        this.barCount = barCount;
    }
}
