package com.globalstock.webapi.model.dto;

import java.util.List;
import java.util.Map;

/**
 * 量化数据概览（管理端）。
 */
public class QuantDataOverviewDTO {

    private String market;
    private String collection;
    private String latestTradeDate;
    private long symbolCount;
    private long historyBarCount;
    private List<Integer> supportedWindows;
    private Map<String, String> periodLabels;

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getLatestTradeDate() {
        return latestTradeDate;
    }

    public void setLatestTradeDate(String latestTradeDate) {
        this.latestTradeDate = latestTradeDate;
    }

    public long getSymbolCount() {
        return symbolCount;
    }

    public void setSymbolCount(long symbolCount) {
        this.symbolCount = symbolCount;
    }

    public long getHistoryBarCount() {
        return historyBarCount;
    }

    public void setHistoryBarCount(long historyBarCount) {
        this.historyBarCount = historyBarCount;
    }

    public List<Integer> getSupportedWindows() {
        return supportedWindows;
    }

    public void setSupportedWindows(List<Integer> supportedWindows) {
        this.supportedWindows = supportedWindows;
    }

    public Map<String, String> getPeriodLabels() {
        return periodLabels;
    }

    public void setPeriodLabels(Map<String, String> periodLabels) {
        this.periodLabels = periodLabels;
    }
}
