package com.globalstock.webapi.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 数据产品目录文档。
 */
@Document("data_products")
public class DataProductDocument {

    @Id
    private String id;
    private String name;
    private String group;

    private String market;

    @Field("capability")
    private String capability;

    private String description;

    @Field("price_amount")
    private Long priceAmount;

    @Field("price_currency")
    private String priceCurrency;

    @Field("period_months")
    private Integer periodMonths;

    private List<String> requires;

    @Field("api_quota_monthly")
    private Long apiQuotaMonthly;

    private String status;

    @Field("sort_order")
    private Integer sortOrder;

    @Field("created_at")
    private Long createdAt;

    @Field("updated_at")
    private Long updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }
    public String getCapability() { return capability; }
    public void setCapability(String capability) { this.capability = capability; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getPriceAmount() { return priceAmount; }
    public void setPriceAmount(Long priceAmount) { this.priceAmount = priceAmount; }
    public String getPriceCurrency() { return priceCurrency; }
    public void setPriceCurrency(String priceCurrency) { this.priceCurrency = priceCurrency; }
    public Integer getPeriodMonths() { return periodMonths; }
    public void setPeriodMonths(Integer periodMonths) { this.periodMonths = periodMonths; }
    public List<String> getRequires() { return requires; }
    public void setRequires(List<String> requires) { this.requires = requires; }
    public Long getApiQuotaMonthly() { return apiQuotaMonthly; }
    public void setApiQuotaMonthly(Long apiQuotaMonthly) { this.apiQuotaMonthly = apiQuotaMonthly; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
