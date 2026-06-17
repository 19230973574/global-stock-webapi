package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.mapper.ApiUsageMapper;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;

@Service
public class ApiUsageDomainService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final ApiUsageMapper apiUsageMapper;

    public ApiUsageDomainService(ApiUsageMapper apiUsageMapper) {
        this.apiUsageMapper = apiUsageMapper;
    }

    public String currentMonth() {
        return YearMonth.now(ZONE).toString();
    }

    public long incrementUsage(String accountEmail) {
        long now = System.currentTimeMillis();
        return apiUsageMapper.incrementAndGet(accountEmail, currentMonth(), now);
    }

    public long getCurrentUsage(String accountEmail) {
        var counter = apiUsageMapper.findByAccountAndMonth(accountEmail, currentMonth());
        return counter == null || counter.getCallCount() == null ? 0 : counter.getCallCount();
    }

    public long sumCallsThisMonth() {
        return apiUsageMapper.sumCallsByMonth(currentMonth());
    }
}
