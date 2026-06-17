package com.globalstock.webapi.model.dto;

import java.util.List;

public class UsDataMgmtOverviewDTO {

    private String market;
    private long stockCount;
    private long klineCodeCount;
    private long historyBarCount;
    private String latestTradeDate;
    private List<KlineDateDistributionItemDTO> dateDistribution;

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public long getStockCount() {
        return stockCount;
    }

    public void setStockCount(long stockCount) {
        this.stockCount = stockCount;
    }

    public long getKlineCodeCount() {
        return klineCodeCount;
    }

    public void setKlineCodeCount(long klineCodeCount) {
        this.klineCodeCount = klineCodeCount;
    }

    public long getHistoryBarCount() {
        return historyBarCount;
    }

    public void setHistoryBarCount(long historyBarCount) {
        this.historyBarCount = historyBarCount;
    }

    public String getLatestTradeDate() {
        return latestTradeDate;
    }

    public void setLatestTradeDate(String latestTradeDate) {
        this.latestTradeDate = latestTradeDate;
    }

    public List<KlineDateDistributionItemDTO> getDateDistribution() {
        return dateDistribution;
    }

    public void setDateDistribution(List<KlineDateDistributionItemDTO> dateDistribution) {
        this.dateDistribution = dateDistribution;
    }

    public static class KlineDateDistributionItemDTO {
        private String date;
        private long count;

        public KlineDateDistributionItemDTO() {
        }

        public KlineDateDistributionItemDTO(String date, long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
