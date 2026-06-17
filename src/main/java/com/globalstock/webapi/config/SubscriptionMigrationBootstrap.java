package com.globalstock.webapi.config;

import com.globalstock.webapi.model.document.UserAccountDocument;
import com.globalstock.webapi.service.domain.AuthDomainService;
import com.globalstock.webapi.service.domain.CatalogDomainService;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 将历史 data_permissions 迁移为订阅记录（幂等）。
 */
@Component
@Order(20)
public class SubscriptionMigrationBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionMigrationBootstrap.class);

    private final AuthDomainService authDomainService;
    private final CatalogDomainService catalogDomainService;
    private final SubscriptionDomainService subscriptionDomainService;

    public SubscriptionMigrationBootstrap(AuthDomainService authDomainService,
                                          CatalogDomainService catalogDomainService,
                                          SubscriptionDomainService subscriptionDomainService) {
        this.authDomainService = authDomainService;
        this.catalogDomainService = catalogDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
    }

    @Override
    public void run(ApplicationArguments args) {
        int migrated = 0;
        for (UserAccountDocument account : authDomainService.findAll()) {
            if (account.getDataPermissions() == null || account.getDataPermissions().isEmpty()) {
                continue;
            }
            for (String permission : account.getDataPermissions()) {
                if (!catalogDomainService.isSupportedProduct(permission)) {
                    continue;
                }
                boolean hasActive = subscriptionDomainService.listActiveByAccount(account.getEmail()).stream()
                        .anyMatch(sub -> permission.equals(sub.getProductId()));
                if (hasActive) {
                    continue;
                }
                subscriptionDomainService.activateSubscription(
                        account.getEmail(), permission, null, null, "migration");
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("subscription migration completed count={}", migrated);
        }
    }
}
