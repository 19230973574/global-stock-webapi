package com.globalstock.webapi.model.document;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * MongoDB 股票基础信息文档（爬虫写入 us_stock_info 等集合）。
 */
public class StockInfoDocument {

    private String code;
    private String market;
    private String symbol;
    private String name;

    @Field("name_cn")
    private String nameCn;

    @Field("circulating_shares")
    private Long circulatingShares;

    @Field("total_shares")
    private Long totalShares;

    @Field("market_cap")
    private Double marketCap;

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

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public Long getCirculatingShares() {
        return circulatingShares;
    }

    public void setCirculatingShares(Long circulatingShares) {
        this.circulatingShares = circulatingShares;
    }

    public Long getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(Long totalShares) {
        this.totalShares = totalShares;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
}
