package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.mapper.SubscriptionMapper;
import com.globalstock.webapi.model.document.DataProductDocument;
import com.globalstock.webapi.model.document.SubscriptionDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SubscriptionDomainService {

    private final SubscriptionMapper subscriptionMapper;
    private final CatalogDomainService catalogDomainService;

    public SubscriptionDomainService(SubscriptionMapper subscriptionMapper,
                                     CatalogDomainService catalogDomainService) {
        this.subscriptionMapper = subscriptionMapper;
        this.catalogDomainService = catalogDomainService;
    }

    public SubscriptionDocument activateSubscription(String accountEmail,
                                                   String productId,
                                                   Long expiresAt,
                                                   String orderId,
                                                   String grantedBy) {
        long now = System.currentTimeMillis();
        SubscriptionDocument document = new SubscriptionDocument();
        document.setAccountEmail(accountEmail);
        document.setProductId(productId);
        document.setStatus("active");
        document.setStartsAt(now);
        document.setExpiresAt(expiresAt);
        document.setOrderId(orderId);
        document.setGrantedBy(grantedBy);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return subscriptionMapper.save(document);
    }

    public void revokeSubscriptions(String accountEmail, String productId) {
        long now = System.currentTimeMillis();
        for (SubscriptionDocument sub : subscriptionMapper.findByAccountEmail(accountEmail)) {
            if (!productId.equals(sub.getProductId()) || !"active".equals(sub.getStatus())) {
                continue;
            }
            sub.setStatus("revoked");
            sub.setUpdatedAt(now);
            subscriptionMapper.save(sub);
        }
    }

    public List<SubscriptionDocument> listByAccount(String accountEmail) {
        return subscriptionMapper.findByAccountEmail(accountEmail);
    }

    public List<SubscriptionDocument> listActiveByAccount(String accountEmail) {
        long now = System.currentTimeMillis();
        return subscriptionMapper.findByAccountEmail(accountEmail).stream()
                .filter(sub -> isActive(sub, now))
                .toList();
    }

    public List<SubscriptionDocument> listExpiredActive() {
        long now = System.currentTimeMillis();
        return subscriptionMapper.findAllActiveWithExpiry().stream()
                .filter(sub -> sub.getExpiresAt() != null && sub.getExpiresAt() <= now)
                .toList();
    }

    public Set<String> resolveActiveProductIds(String accountEmail) {
        long now = System.currentTimeMillis();
        Set<String> productIds = new LinkedHashSet<>();
        for (SubscriptionDocument sub : subscriptionMapper.findByAccountEmail(accountEmail)) {
            if (!isActive(sub, now)) {
                continue;
            }
            productIds.add(sub.getProductId());
            catalogDomainService.findProduct(sub.getProductId()).ifPresent(product -> {
                if (product.getRequires() != null) {
                    productIds.addAll(product.getRequires());
                }
            });
        }
        return productIds;
    }

    public Long resolveMonthlyQuota(String accountEmail) {
        long now = System.currentTimeMillis();
        Long maxQuota = null;
        boolean unlimited = false;
        for (SubscriptionDocument sub : subscriptionMapper.findByAccountEmail(accountEmail)) {
            if (!isActive(sub, now)) {
                continue;
            }
            DataProductDocument product = catalogDomainService.findProduct(sub.getProductId()).orElse(null);
            if (product == null || product.getApiQuotaMonthly() == null) {
                unlimited = true;
                break;
            }
            maxQuota = maxQuota == null
                    ? product.getApiQuotaMonthly()
                    : Math.max(maxQuota, product.getApiQuotaMonthly());
        }
        return unlimited ? null : maxQuota;
    }

    public void expireSubscription(SubscriptionDocument subscription) {
        subscription.setStatus("expired");
        subscription.setUpdatedAt(System.currentTimeMillis());
        subscriptionMapper.save(subscription);
    }

    public long countActiveSubscriptions() {
        long now = System.currentTimeMillis();
        return subscriptionMapper.findByAccountEmailAll().stream()
                .filter(sub -> isActive(sub, now))
                .count();
    }

    public long countExpiringWithinDays(int days) {
        long now = System.currentTimeMillis();
        long deadline = now + days * 24L * 60L * 60L * 1000L;
        return subscriptionMapper.findAllActiveWithExpiry().stream()
                .filter(sub -> sub.getExpiresAt() != null && sub.getExpiresAt() > now && sub.getExpiresAt() <= deadline)
                .count();
    }

    private boolean isActive(SubscriptionDocument sub, long now) {
        if (!"active".equals(sub.getStatus())) {
            return false;
        }
        if (sub.getExpiresAt() == null) {
            return true;
        }
        return sub.getExpiresAt() > now;
    }
}
