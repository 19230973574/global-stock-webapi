package com.globalstock.webapi.model.dto;

import java.util.List;

public class KlineRebuildResultDTO {

    private boolean preview;
    private long previewDeleteCount;
    private long deletedCount;
    private boolean allCodes;
    private int codeCount;
    private String startDate;
    private String endDate;
    private int taskCount;
    private List<String> taskIds;
    private List<String> codesPreview;

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public long getPreviewDeleteCount() {
        return previewDeleteCount;
    }

    public void setPreviewDeleteCount(long previewDeleteCount) {
        this.previewDeleteCount = previewDeleteCount;
    }

    public long getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(long deletedCount) {
        this.deletedCount = deletedCount;
    }

    public boolean isAllCodes() {
        return allCodes;
    }

    public void setAllCodes(boolean allCodes) {
        this.allCodes = allCodes;
    }

    public int getCodeCount() {
        return codeCount;
    }

    public void setCodeCount(int codeCount) {
        this.codeCount = codeCount;
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

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public List<String> getCodesPreview() {
        return codesPreview;
    }

    public void setCodesPreview(List<String> codesPreview) {
        this.codesPreview = codesPreview;
    }
}
