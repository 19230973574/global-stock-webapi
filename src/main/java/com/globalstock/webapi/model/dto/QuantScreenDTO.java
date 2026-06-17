package com.globalstock.webapi.model.dto;

import java.util.List;
import java.util.Map;

/**
 * 量化专区筛选结果汇总。
 */
public class QuantScreenDTO {

    private String tradeDate;
    private List<QuantSignalDTO> newHighs;
    private List<QuantSignalDTO> newLows;
    private Map<String, List<QuantSignalDTO>> periodHighs;

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public List<QuantSignalDTO> getNewHighs() {
        return newHighs;
    }

    public void setNewHighs(List<QuantSignalDTO> newHighs) {
        this.newHighs = newHighs;
    }

    public List<QuantSignalDTO> getNewLows() {
        return newLows;
    }

    public void setNewLows(List<QuantSignalDTO> newLows) {
        this.newLows = newLows;
    }

    public Map<String, List<QuantSignalDTO>> getPeriodHighs() {
        return periodHighs;
    }

    public void setPeriodHighs(Map<String, List<QuantSignalDTO>> periodHighs) {
        this.periodHighs = periodHighs;
    }
}
