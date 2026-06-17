package com.globalstock.webapi.model.dto;

import java.util.List;

/**
 * 量化筛选分页结果。
 */
public class QuantScreenPageDTO {

    private String tradeDate;
    private String signalType;
    private String period;
    private Integer windowDays;
    private int page;
    private int pageSize;
    private long total;
    private Double minMarketCap;
    private Double maxMarketCap;
    private String sortBy;
    private String sortOrder;
    private List<QuantSignalDTO> items;

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getWindowDays() {
        return windowDays;
    }

    public void setWindowDays(Integer windowDays) {
        this.windowDays = windowDays;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Double getMinMarketCap() {
        return minMarketCap;
    }

    public void setMinMarketCap(Double minMarketCap) {
        this.minMarketCap = minMarketCap;
    }

    public Double getMaxMarketCap() {
        return maxMarketCap;
    }

    public void setMaxMarketCap(Double maxMarketCap) {
        this.maxMarketCap = maxMarketCap;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<QuantSignalDTO> getItems() {
        return items;
    }

    public void setItems(List<QuantSignalDTO> items) {
        this.items = items;
    }
}
