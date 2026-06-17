package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.mapper.ApiCallLogMapper;
import com.globalstock.webapi.model.document.ApiCallLogDocument;
import com.globalstock.webapi.service.domain.ApiUsageDomainService;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * API 调用计量与配额校验。
 */
@Service
public class UsageService {

    private static final Logger log = LoggerFactory.getLogger(UsageService.class);

    private final SubscriptionDomainService subscriptionDomainService;
    private final ApiUsageDomainService apiUsageDomainService;
    private final ApiCallLogMapper apiCallLogMapper;

    public UsageService(SubscriptionDomainService subscriptionDomainService,
                        ApiUsageDomainService apiUsageDomainService,
                        ApiCallLogMapper apiCallLogMapper) {
        this.subscriptionDomainService = subscriptionDomainService;
        this.apiUsageDomainService = apiUsageDomainService;
        this.apiCallLogMapper = apiCallLogMapper;
    }

    public void assertWithinQuota(String accountEmail) {
        if (accountEmail == null || accountEmail.isBlank()) {
            return;
        }
        Long quota = subscriptionDomainService.resolveMonthlyQuota(accountEmail);
        if (quota == null) {
            return;
        }
        long used = apiUsageDomainService.getCurrentUsage(accountEmail);
        if (used >= quota) {
            throw new BusinessException("本月 API 调用额度已用尽（" + quota + " 次）");
        }
    }

    public void recordCall(String accountEmail, String tokenValue, String endpoint, String market, String code) {
        if (accountEmail == null || accountEmail.isBlank()) {
            return;
        }
        long now = System.currentTimeMillis();
        String monthKey = apiUsageDomainService.currentMonth();
        long count = apiUsageDomainService.incrementUsage(accountEmail);

        ApiCallLogDocument logDoc = new ApiCallLogDocument();
        logDoc.setAccountEmail(accountEmail);
        logDoc.setTokenValue(tokenValue);
        logDoc.setEndpoint(endpoint);
        logDoc.setMarket(market);
        logDoc.setCode(code);
        logDoc.setMonthKey(monthKey);
        logDoc.setCreatedAt(now);
        apiCallLogMapper.save(logDoc);

        log.debug("api usage recorded email={}, endpoint={}, monthCount={}", accountEmail, endpoint, count);
    }

    public long countTokenUsageThisMonth(String tokenValue) {
        return apiCallLogMapper.countByTokenAndMonth(tokenValue, apiUsageDomainService.currentMonth());
    }

    public long countAccountUsageThisMonth(String accountEmail) {
        return apiCallLogMapper.countByAccountAndMonth(accountEmail, apiUsageDomainService.currentMonth());
    }

    public String getCurrentMonth() {
        return apiUsageDomainService.currentMonth();
    }
}
