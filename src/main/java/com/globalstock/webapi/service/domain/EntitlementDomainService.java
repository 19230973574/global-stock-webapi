package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.model.document.UserAccountDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

/**
 * 数据权限生效领域服务。
 */
@Service
public class EntitlementDomainService {

    private final AuthDomainService authDomainService;
    private final CatalogDomainService catalogDomainService;
    private final SubscriptionDomainService subscriptionDomainService;

    public EntitlementDomainService(AuthDomainService authDomainService,
                                    CatalogDomainService catalogDomainService,
                                    SubscriptionDomainService subscriptionDomainService) {
        this.authDomainService = authDomainService;
        this.catalogDomainService = catalogDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
    }

    /**
     * 管理员直接开通：创建永久订阅并同步权限。
     */
    public void grantPermission(UserAccountDocument account, String permission, String grantedBy) {
        catalogDomainService.normalizeProductId(permission);
        subscriptionDomainService.activateSubscription(
                account.getEmail(), permission, null, null, grantedBy);
        syncFromSubscriptions(account);
    }

    /**
     * 管理员直接撤销：作废订阅并同步权限。
     */
    public void revokePermission(UserAccountDocument account, String permission) {
        catalogDomainService.normalizeProductId(permission);
        subscriptionDomainService.revokeSubscriptions(account.getEmail(), permission);
        syncFromSubscriptions(account);
    }

    /**
     * 订单审核通过：按产品周期创建订阅并同步权限。
     */
    public void grantFromOrder(UserAccountDocument account, String permission,
                               String orderId, String grantedBy) {
        if (catalogDomainService.isBundlePermission(permission)) {
            grantBundleFromOrder(account, catalogDomainService.extractBundleId(permission), orderId, grantedBy);
            return;
        }
        var product = catalogDomainService.requireProduct(permission);
        Long expiresAt = null;
        if (product.getPeriodMonths() != null && product.getPeriodMonths() > 0) {
            expiresAt = System.currentTimeMillis()
                    + product.getPeriodMonths() * 30L * 24L * 60L * 60L * 1000L;
        }
        subscriptionDomainService.activateSubscription(
                account.getEmail(), permission, expiresAt, orderId, grantedBy);
        syncFromSubscriptions(account);
    }

    public void grantBundleFromOrder(UserAccountDocument account, String bundleId,
                                     String orderId, String grantedBy) {
        var bundle = catalogDomainService.requireBundle(bundleId);
        if (bundle.getProductIds() == null || bundle.getProductIds().isEmpty()) {
            throw new BusinessException("套餐未包含任何数据产品");
        }
        for (String productId : bundle.getProductIds()) {
            if (hasPermission(account, productId)) {
                continue;
            }
            var product = catalogDomainService.requireProduct(productId);
            Long expiresAt = null;
            if (product.getPeriodMonths() != null && product.getPeriodMonths() > 0) {
                expiresAt = System.currentTimeMillis()
                        + product.getPeriodMonths() * 30L * 24L * 60L * 60L * 1000L;
            }
            subscriptionDomainService.activateSubscription(
                    account.getEmail(), productId, expiresAt, orderId, grantedBy);
            syncFromSubscriptions(account);
        }
    }

    public void syncFromSubscriptions(UserAccountDocument account) {
        Set<String> productIds = subscriptionDomainService.resolveActiveProductIds(account.getEmail());
        account.setDataPermissions(new ArrayList<>(productIds));
        authDomainService.save(account);
    }

    public boolean hasPermission(UserAccountDocument account, String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }
        String normalized = permission.trim().toLowerCase(java.util.Locale.ROOT);
        if (!catalogDomainService.isSupportedProduct(normalized)) {
            return false;
        }
        return account.getDataPermissions() != null && account.getDataPermissions().contains(normalized);
    }
}
