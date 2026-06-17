package com.globalstock.webapi.service;

import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.QuantDataOverviewDTO;
import com.globalstock.webapi.model.dto.QuantScreenDTO;
import com.globalstock.webapi.model.dto.QuantScreenPageDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminQuantService {

    private final AuthService authService;
    private final QuantService quantService;

    public AdminQuantService(AuthService authService, QuantService quantService) {
        this.authService = authService;
        this.quantService = quantService;
    }

    public QuantDataOverviewDTO getUsOverview() {
        authService.requireAdminRole();
        return quantService.getUsOverview();
    }

    public QuantScreenDTO screenUsMarket(Integer days, Integer limit, String code,
                                         Double minMarketCap, Double maxMarketCap) {
        authService.requireAdminRole();
        return quantService.screenUsMarketData(days, limit, code, minMarketCap, maxMarketCap);
    }

    public QuantScreenPageDTO screenUsMarketPage(String signalType, Integer days, String period,
                                                 Integer page, Integer pageSize, String code,
                                                 Double minMarketCap, Double maxMarketCap,
                                                 String sortBy, String sortOrder) {
        authService.requireAdminRole();
        return quantService.screenUsMarketPage(
                signalType, days, period, page, pageSize, code, minMarketCap, maxMarketCap,
                sortBy, sortOrder, false);
    }

    public List<KlineBarDTO> getUsHistoryBars(String code, Integer limit) {
        authService.requireAdminRole();
        return quantService.getUsHistoryBars(code, limit, false);
    }
}
