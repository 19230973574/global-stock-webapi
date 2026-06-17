package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.model.document.ApiTokenDocument;
import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.QuoteDTO;
import com.globalstock.webapi.model.request.MarketDataQuery;
import com.globalstock.webapi.service.domain.ApiTokenDomainService;
import com.globalstock.webapi.service.domain.CatalogDomainService;
import com.globalstock.webapi.service.domain.MarketDataDomainService;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private static final Set<String> SUPPORTED_MARKETS = Set.of("US", "A", "HK", "ETF");
    private static final Set<String> SUPPORTED_INTRADAY_PERIODS = Set.of("1m", "5m", "15m", "30m", "1h");
    private static final Set<String> SUPPORTED_HISTORY_PERIODS = Set.of("1d", "1w", "1M");

    private final MarketDataDomainService marketDataDomainService;
    private final ApiTokenDomainService apiTokenDomainService;
    private final CatalogDomainService catalogDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final UsageService usageService;

    public MarketDataService(MarketDataDomainService marketDataDomainService,
                             ApiTokenDomainService apiTokenDomainService,
                             CatalogDomainService catalogDomainService,
                             SubscriptionDomainService subscriptionDomainService,
                             UsageService usageService) {
        this.marketDataDomainService = marketDataDomainService;
        this.apiTokenDomainService = apiTokenDomainService;
        this.catalogDomainService = catalogDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.usageService = usageService;
    }

    public QuoteDTO getQuote(MarketDataQuery query) {
        long start = System.currentTimeMillis();
        log.info("get quote start market={}, code={}", query.market(), query.code());
        try {
            String market = normalizeMarket(query.market());
            String code = normalizeCode(query.code());
            ApiTokenDocument token = verifyPermission(query.apiToken(), market, "realtime");
            usageService.assertWithinQuota(token.getAccountEmail());
            QuoteDTO quote = marketDataDomainService.getQuote(market, code);
            if (quote == null) {
                throw new BusinessException("实时行情不存在");
            }
            usageService.recordCall(token.getAccountEmail(), token.getTokenValue(), "quotes", market, code);
            log.info("get quote success market={}, code={}, cost={}ms", market, code, System.currentTimeMillis() - start);
            return quote;
        } catch (BusinessException exception) {
            log.warn("get quote business failed market={}, code={}, message={}",
                    query.market(), query.code(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("get quote system failed market={}, code={}", query.market(), query.code(), exception);
            throw exception;
        }
    }

    public List<KlineBarDTO> getIntradayBars(MarketDataQuery query) {
        long start = System.currentTimeMillis();
        log.info("get intraday bars start market={}, code={}, period={}, limit={}",
                query.market(), query.code(), query.period(), query.limit());
        try {
            String market = normalizeMarket(query.market());
            String code = normalizeCode(query.code());
            String period = normalizePeriod(query.period(), "1m", SUPPORTED_INTRADAY_PERIODS);
            int limit = normalizeLimit(query.limit(), 240);
            ApiTokenDocument token = verifyPermission(query.apiToken(), market, "timeshare");
            usageService.assertWithinQuota(token.getAccountEmail());
            List<KlineBarDTO> bars = marketDataDomainService.getIntradayBars(market, code, period, limit);
            usageService.recordCall(token.getAccountEmail(), token.getTokenValue(), "intraday-bars", market, code);
            log.info("get intraday bars success market={}, code={}, count={}, cost={}ms",
                    market, code, bars.size(), System.currentTimeMillis() - start);
            return bars;
        } catch (BusinessException exception) {
            log.warn("get intraday bars business failed market={}, code={}, message={}",
                    query.market(), query.code(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("get intraday bars system failed market={}, code={}", query.market(), query.code(), exception);
            throw exception;
        }
    }

    public List<KlineBarDTO> getHistoryBars(MarketDataQuery query) {
        long start = System.currentTimeMillis();
        log.info("get history bars start market={}, code={}, period={}, limit={}",
                query.market(), query.code(), query.period(), query.limit());
        try {
            String market = normalizeMarket(query.market());
            String code = normalizeCode(query.code());
            String period = normalizePeriod(query.period(), "1d", SUPPORTED_HISTORY_PERIODS);
            int limit = normalizeLimit(query.limit(), 100);
            ApiTokenDocument token = verifyPermission(query.apiToken(), market, "history");
            usageService.assertWithinQuota(token.getAccountEmail());
            List<KlineBarDTO> bars = marketDataDomainService.getHistoryBars(market, code, period, limit);
            usageService.recordCall(token.getAccountEmail(), token.getTokenValue(), "history-bars", market, code);
            log.info("get history bars success market={}, code={}, count={}, cost={}ms",
                    market, code, bars.size(), System.currentTimeMillis() - start);
            return bars;
        } catch (BusinessException exception) {
            log.warn("get history bars business failed market={}, code={}, message={}",
                    query.market(), query.code(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("get history bars system failed market={}, code={}", query.market(), query.code(), exception);
            throw exception;
        }
    }

    private String normalizeMarket(String market) {
        String normalized = market == null || market.isBlank() ? "US" : market.toUpperCase(Locale.ROOT);
        if (!SUPPORTED_MARKETS.contains(normalized)) {
            throw new BusinessException("不支持的市场");
        }
        return normalized;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("股票代码不能为空");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        // Mongo 中美股/港股多为裸代码（AAPL），客户端常带后缀（AAPL.US）
        if (normalized.endsWith(".US") || normalized.endsWith(".HK")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        return normalized;
    }

    private String normalizePeriod(String period, String defaultPeriod, Set<String> supportedPeriods) {
        String normalized = period == null || period.isBlank() ? defaultPeriod : period;
        if (!supportedPeriods.contains(normalized)) {
            throw new BusinessException("不支持的K线周期");
        }
        return normalized;
    }

    private int normalizeLimit(Integer limit, int defaultLimit) {
        if (limit == null) {
            return defaultLimit;
        }
        if (limit < 1 || limit > 1000) {
            throw new BusinessException("limit 必须在 1 到 1000 之间");
        }
        return limit;
    }

    private ApiTokenDocument verifyPermission(String apiToken, String market, String capability) {
        if (apiToken == null || apiToken.isBlank()) {
            throw new BusinessException("请先生成并携带 API Token");
        }
        ApiTokenDocument token = apiTokenDomainService.findByTokenValue(apiToken.trim());
        if (token == null || !"active".equals(token.getStatus())) {
            throw new BusinessException("API Token 无效");
        }
        if (token.getExpiresAt() != null && token.getExpiresAt() < System.currentTimeMillis()) {
            throw new BusinessException("API Token 已过期");
        }

        Set<String> livePermissions = subscriptionDomainService.resolveActiveProductIds(token.getAccountEmail());

        catalogDomainService.resolveMarketOpenPermission(market).ifPresent(openPerm -> {
            if (!livePermissions.contains(openPerm)) {
                throw new BusinessException("当前账号未开通 " + openPerm + " 权限");
            }
        });

        String requiredPermission = catalogDomainService.resolveRequiredPermission(market, capability);
        if (!livePermissions.contains(requiredPermission)) {
            throw new BusinessException("当前账号未开通 " + requiredPermission + " 权限");
        }
        return token;
    }
}
