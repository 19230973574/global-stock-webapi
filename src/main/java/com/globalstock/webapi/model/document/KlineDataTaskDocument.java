package com.globalstock.webapi.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * K 线数据任务文档（拉取任务由 Python worker 消费）。
 */
@Document("kline_data_tasks")
public class KlineDataTaskDocument {

    @Id
    private String id;

    private String type;

    private String status;

    private String market;

    private String period;

    private List<String> codes = new ArrayList<>();

    @Field("start_date")
    private String startDate;

    @Field("end_date")
    private String endDate;

    private KlineTaskProgress progress;

    private KlineTaskResult result;

    @Field("created_by")
    private String createdBy;

    @Field("created_at")
    private Long createdAt;

    @Field("started_at")
    private Long startedAt;

    @Field("finished_at")
    private Long finishedAt;

    @Field("updated_at")
    private Long updatedAt;

    @Field("worker_id")
    private String workerId;

    @Field("error_message")
    private String errorMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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

    public KlineTaskProgress getProgress() {
        return progress;
    }

    public void setProgress(KlineTaskProgress progress) {
        this.progress = progress;
    }

    public KlineTaskResult getResult() {
        return result;
    }

    public void setResult(KlineTaskResult result) {
        this.result = result;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Long startedAt) {
        this.startedAt = startedAt;
    }

    public Long getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Long finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public static class KlineTaskProgress {

        @Field("total_codes")
        private int totalCodes;

        @Field("done_codes")
        private int doneCodes;

        @Field("current_code")
        private String currentCode;

        @Field("saved_bars")
        private int savedBars;

        @Field("failed_codes")
        private int failedCodes;

        public int getTotalCodes() {
            return totalCodes;
        }

        public void setTotalCodes(int totalCodes) {
            this.totalCodes = totalCodes;
        }

        public int getDoneCodes() {
            return doneCodes;
        }

        public void setDoneCodes(int doneCodes) {
            this.doneCodes = doneCodes;
        }

        public String getCurrentCode() {
            return currentCode;
        }

        public void setCurrentCode(String currentCode) {
            this.currentCode = currentCode;
        }

        public int getSavedBars() {
            return savedBars;
        }

        public void setSavedBars(int savedBars) {
            this.savedBars = savedBars;
        }

        public int getFailedCodes() {
            return failedCodes;
        }

        public void setFailedCodes(int failedCodes) {
            this.failedCodes = failedCodes;
        }
    }

    public static class KlineTaskResult {

        @Field("saved_bars")
        private int savedBars;

        @Field("failed_codes")
        private List<String> failedCodes = new ArrayList<>();

        @Field("error_summary")
        private String errorSummary;

        public int getSavedBars() {
            return savedBars;
        }

        public void setSavedBars(int savedBars) {
            this.savedBars = savedBars;
        }

        public List<String> getFailedCodes() {
            return failedCodes;
        }

        public void setFailedCodes(List<String> failedCodes) {
            this.failedCodes = failedCodes;
        }

        public String getErrorSummary() {
            return errorSummary;
        }

        public void setErrorSummary(String errorSummary) {
            this.errorSummary = errorSummary;
        }
    }
}
