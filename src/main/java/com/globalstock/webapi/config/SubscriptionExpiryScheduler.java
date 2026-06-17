package com.globalstock.webapi.config;

import com.globalstock.webapi.model.document.SubscriptionDocument;
import com.globalstock.webapi.model.document.UserAccountDocument;
import com.globalstock.webapi.service.TokenService;
import com.globalstock.webapi.service.domain.AuthDomainService;
import com.globalstock.webapi.service.domain.EntitlementDomainService;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SubscriptionExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionExpiryScheduler.class);

    private final SubscriptionDomainService subscriptionDomainService;
    private final AuthDomainService authDomainService;
    private final EntitlementDomainService entitlementDomainService;
    private final TokenService tokenService;

    public SubscriptionExpiryScheduler(SubscriptionDomainService subscriptionDomainService,
                                       AuthDomainService authDomainService,
                                       EntitlementDomainService entitlementDomainService,
                                       TokenService tokenService) {
        this.subscriptionDomainService = subscriptionDomainService;
        this.authDomainService = authDomainService;
        this.entitlementDomainService = entitlementDomainService;
        this.tokenService = tokenService;
    }

    @Scheduled(fixedDelay = 300_000, initialDelay = 60_000)
    public void expireSubscriptions() {
        var expired = subscriptionDomainService.listExpiredActive();
        if (expired.isEmpty()) {
            return;
        }
        Set<String> affectedEmails = new HashSet<>();
        for (SubscriptionDocument subscription : expired) {
            subscriptionDomainService.expireSubscription(subscription);
            affectedEmails.add(subscription.getAccountEmail());
        }
        for (String email : affectedEmails) {
            UserAccountDocument account = authDomainService.findByEmail(email);
            if (account != null) {
                entitlementDomainService.syncFromSubscriptions(account);
                tokenService.refreshAllForAccount(email);
            }
        }
        log.info("expired subscriptions processed count={}, accounts={}", expired.size(), affectedEmails.size());
    }
}
