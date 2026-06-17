package com.globalstock.webapi.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 用户账号文档。
 *
 * <p>职责说明：映射 MongoDB user_accounts 集合，字段使用下划线命名。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Document("user_accounts")
public class UserAccountDocument {

    @Id
    private String id;
    private String email;
    private String name;

    @Field("password_hash")
    private String passwordHash;

    private String status;

    private String role;

    @Field("data_permissions")
    private List<String> dataPermissions;

    @Field("last_login_at")
    private Long lastLoginAt;

    @Field("activation_code")
    private String activationCode;

    @Field("activation_expires_at")
    private Long activationExpiresAt;

    @Field("reset_token")
    private String resetToken;

    @Field("reset_expires_at")
    private Long resetExpiresAt;

    @Field("created_at")
    private Long createdAt;

    @Field("updated_at")
    private Long updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public List<String> getDataPermissions() { return dataPermissions; }
    public Long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Long lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public void setDataPermissions(List<String> dataPermissions) { this.dataPermissions = dataPermissions; }
    public String getActivationCode() { return activationCode; }
    public void setActivationCode(String activationCode) { this.activationCode = activationCode; }
    public Long getActivationExpiresAt() { return activationExpiresAt; }
    public void setActivationExpiresAt(Long activationExpiresAt) { this.activationExpiresAt = activationExpiresAt; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public Long getResetExpiresAt() { return resetExpiresAt; }
    public void setResetExpiresAt(Long resetExpiresAt) { this.resetExpiresAt = resetExpiresAt; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
