package com.globalstock.webapi.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 行情 API 调用日志。
 */
@Document("api_call_logs")
public class ApiCallLogDocument {

    @Id
    private String id;

    @Field("account_email")
    private String accountEmail;

    @Field("token_value")
    private String tokenValue;

    private String endpoint;

    private String market;

    private String code;

    @Field("month_key")
    private String monthKey;

    @Field("created_at")
    private Long createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAccountEmail() { return accountEmail; }
    public void setAccountEmail(String accountEmail) { this.accountEmail = accountEmail; }
    public String getTokenValue() { return tokenValue; }
    public void setTokenValue(String tokenValue) { this.tokenValue = tokenValue; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMonthKey() { return monthKey; }
    public void setMonthKey(String monthKey) { this.monthKey = monthKey; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
