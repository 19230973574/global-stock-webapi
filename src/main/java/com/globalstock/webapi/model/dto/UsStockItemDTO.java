package com.globalstock.webapi.model.dto;

public class UsStockItemDTO {

    private String code;
    private String symbol;
    private String name;
    private Double marketCap;
    private String klineLatestDate;
    private Long klineBarCount;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public String getKlineLatestDate() {
        return klineLatestDate;
    }

    public void setKlineLatestDate(String klineLatestDate) {
        this.klineLatestDate = klineLatestDate;
    }

    public Long getKlineBarCount() {
        return klineBarCount;
    }

    public void setKlineBarCount(Long klineBarCount) {
        this.klineBarCount = klineBarCount;
    }
}
