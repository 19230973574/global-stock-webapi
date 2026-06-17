package com.globalstock.webapi.model.dto;

/**
 * 量化筛选信号。
 */
public class QuantSignalDTO {

    private String code;
    private String market;
    private String tradeDate;
    private Double close;
    private Double high;
    private Double low;
    private Integer windowDays;
    private String period;
    private String signalType;
    private Double priorExtreme;
    private Double breakPct;
    private Double marketCap;
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Integer getWindowDays() {
        return windowDays;
    }

    public void setWindowDays(Integer windowDays) {
        this.windowDays = windowDays;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getSignalType() {
        return signalType;
    }

    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    public Double getPriorExtreme() {
        return priorExtreme;
    }

    public void setPriorExtreme(Double priorExtreme) {
        this.priorExtreme = priorExtreme;
    }

    public Double getBreakPct() {
        return breakPct;
    }

    public void setBreakPct(Double breakPct) {
        this.breakPct = breakPct;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
