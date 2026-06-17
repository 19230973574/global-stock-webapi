package com.globalstock.webapi.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * API Token 文档。
 *
 * <p>职责说明：映射 MongoDB api_tokens 集合，记录账号 Token 与权限快照。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Document("api_tokens")
public class ApiTokenDocument {

    @Id
    private String id;

    @Field("token_value")
    private String tokenValue;

    @Field("account_email")
    private String accountEmail;

    @Field("data_permissions")
    private List<String> dataPermissions;

    private String status;

    @Field("created_at")
    private Long createdAt;

    @Field("expires_at")
    private Long expiresAt;

    @Field("updated_at")
    private Long updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }
    public String getAccountEmail() { return accountEmail; }
    public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }
    public List<String> getDataPermissions() { return dataPermissions; }
    public void setDataPermissions(List<String> dataPermissions) { this.dataPermissions = dataPermissions; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
