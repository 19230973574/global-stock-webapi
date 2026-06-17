package com.globalstock.webapi.model.dto;

public class KlineTradeDateItemDTO {

    private String date;
    private long barCount;
    /** 同步状态：PENDING / RUNNING，无任务时为 null */
    private String syncStatus;
    /** 覆盖该日的任务日期区间 */
    private String syncTaskStartDate;
    private String syncTaskEndDate;
    /** 任务实际开始 / 结束时间戳（毫秒） */
    private Long syncStartedAt;
    private Long syncFinishedAt;

    public KlineTradeDateItemDTO() {
    }

    public KlineTradeDateItemDTO(String date, long barCount) {
        this.date = date;
        this.barCount = barCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getBarCount() {
        return barCount;
    }

    public void setBarCount(long barCount) {
        this.barCount = barCount;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getSyncTaskStartDate() {
        return syncTaskStartDate;
    }

    public void setSyncTaskStartDate(String syncTaskStartDate) {
        this.syncTaskStartDate = syncTaskStartDate;
    }

    public String getSyncTaskEndDate() {
        return syncTaskEndDate;
    }

    public void setSyncTaskEndDate(String syncTaskEndDate) {
        this.syncTaskEndDate = syncTaskEndDate;
    }

    public Long getSyncStartedAt() {
        return syncStartedAt;
    }

    public void setSyncStartedAt(Long syncStartedAt) {
        this.syncStartedAt = syncStartedAt;
    }

    public Long getSyncFinishedAt() {
        return syncFinishedAt;
    }

    public void setSyncFinishedAt(Long syncFinishedAt) {
        this.syncFinishedAt = syncFinishedAt;
    }
}
