package com.globalstock.webapi.model.dto;

import java.util.List;

public class KlineDeleteResultDTO {

    private long deletedCount;
    private long previewCount;
    private List<String> codes;
    private String startDate;
    private String endDate;
    private boolean preview;

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public long getPreviewCount() {
        return previewCount;
    }

    public void setPreviewCount(long previewCount) {
        this.previewCount = previewCount;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }
}
