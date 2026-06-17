package com.globalstock.webapi.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 产品套餐文档（对齐官网定价页）。
 */
@Document("product_bundles")
public class ProductBundleDocument {

    @Id
    private String id;
    private String name;
    private String description;

    @Field("product_ids")
    private List<String> productIds;

    @Field("price_amount")
    private Long priceAmount;

    @Field("price_currency")
    private String priceCurrency;

    @Field("price_label")
    private String priceLabel;

    @Field("api_quota_monthly")
    private Long apiQuotaMonthly;

    private boolean popular;
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getProductIds() { return productIds; }
    public void setProductIds(List<String> productIds) { this.productIds = productIds; }
    public Long getPriceAmount() { return priceAmount; }
    public void setPriceAmount(Long priceAmount) { this.priceAmount = priceAmount; }
    public String getPriceCurrency() { return priceCurrency; }
    public void setPriceCurrency(String priceCurrency) { this.priceCurrency = priceCurrency; }
    public String getPriceLabel() { return priceLabel; }
    public void setPriceLabel(String priceLabel) { this.priceLabel = priceLabel; }
    public Long getApiQuotaMonthly() { return apiQuotaMonthly; }
    public void setApiQuotaMonthly(Long apiQuotaMonthly) { this.apiQuotaMonthly = apiQuotaMonthly; }
    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
}
