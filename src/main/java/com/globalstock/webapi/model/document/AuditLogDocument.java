package com.globalstock.webapi.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 操作审计日志文档。
 */
@Document("audit_logs")
public class AuditLogDocument {

    @Id
    private String id;

    private String action;

    @Field("operator_email")
    private String operatorEmail;

    @Field("target_email")
    private String targetEmail;

    @Field("resource_type")
    private String resourceType;

    @Field("resource_id")
    private String resourceId;

    private String detail;

    private String before;

    private String after;

    @Field("created_at")
    private Long createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOperatorEmail() { return operatorEmail; }
    public void setOperatorEmail(String operatorEmail) { this.operatorEmail = operatorEmail; }
    public String getTargetEmail() { return targetEmail; }
    public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getBefore() { return before; }
    public void setBefore(String before) { this.before = before; }
    public String getAfter() { return after; }
    public void setAfter(String after) { this.after = after; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
