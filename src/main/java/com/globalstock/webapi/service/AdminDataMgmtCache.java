package com.globalstock.webapi.service;

import com.globalstock.webapi.model.dto.KlineHistoryBarPageDTO;
import com.globalstock.webapi.model.dto.KlineTradeDatePageDTO;
import com.globalstock.webapi.model.dto.UsDataMgmtOverviewDTO;
import com.globalstock.webapi.model.dto.UsStockPageDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 数据管理模块第一页 / 概览的进程内缓存。前端传 refresh=true 时失效并重新加载。
 */
@Component
public class AdminDataMgmtCache {

    private static final Logger log = LoggerFactory.getLogger(AdminDataMgmtCache.class);
    private static final String OVERVIEW_KEY = "us:overview";
    private static final String STOCKS_FIRST_PAGE_PREFIX = "us:stocks:p1:ps";
    private static final String TRADE_DATES_FIRST_PAGE_PREFIX = "us:dates:p1:";
    private static final String BARS_FIRST_PAGE_PREFIX = "us:bars:p1:ps";

    private final Cache<String, UsDataMgmtOverviewDTO> overviewCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(4)
            .build();

    private final Cache<String, UsStockPageDTO> stocksFirstPageCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(16)
            .build();

    private final Cache<String, KlineTradeDatePageDTO> tradeDatesFirstPageCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(64)
            .build();

    private final Cache<String, KlineHistoryBarPageDTO> barsFirstPageCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(16)
            .build();

    public UsDataMgmtOverviewDTO getOverview(Supplier<UsDataMgmtOverviewDTO> loader, boolean refresh) {
        if (refresh) {
            overviewCache.invalidate(OVERVIEW_KEY);
            log.debug("data-mgmt overview cache invalidated by refresh");
        }
        return overviewCache.get(OVERVIEW_KEY, key -> {
            log.debug("data-mgmt overview cache miss, loading");
            return loader.get();
        });
    }

    public UsStockPageDTO getStocksFirstPage(int pageSize, Supplier<UsStockPageDTO> loader, boolean refresh) {
        String cacheKey = STOCKS_FIRST_PAGE_PREFIX + pageSize;
        if (refresh) {
            stocksFirstPageCache.invalidate(cacheKey);
            log.debug("data-mgmt stocks first page cache invalidated key={}", cacheKey);
        }
        return stocksFirstPageCache.get(cacheKey, key -> {
            log.debug("data-mgmt stocks first page cache miss key={}", cacheKey);
            return loader.get();
        });
    }

    public KlineTradeDatePageDTO getTradeDatesFirstPage(String cacheKey, Supplier<KlineTradeDatePageDTO> loader,
                                                        boolean refresh) {
        if (refresh) {
            tradeDatesFirstPageCache.invalidate(cacheKey);
            log.debug("data-mgmt trade dates first page cache invalidated key={}", cacheKey);
        }
        return tradeDatesFirstPageCache.get(cacheKey, key -> {
            log.debug("data-mgmt trade dates first page cache miss key={}", cacheKey);
            return loader.get();
        });
    }

    public static String tradeDatesFirstPageKey(int pageSize, String startDate, String endDate) {
        return TRADE_DATES_FIRST_PAGE_PREFIX + "ps" + pageSize
                + ":s" + (startDate == null ? "ALL" : startDate)
                + ":e" + (endDate == null ? "ALL" : endDate);
    }

    public KlineHistoryBarPageDTO getBarsFirstPage(int pageSize, Supplier<KlineHistoryBarPageDTO> loader,
                                                   boolean refresh) {
        String cacheKey = BARS_FIRST_PAGE_PREFIX + pageSize;
        if (refresh) {
            barsFirstPageCache.invalidate(cacheKey);
            log.debug("data-mgmt bars first page cache invalidated key={}", cacheKey);
        }
        return barsFirstPageCache.get(cacheKey, key -> {
            log.debug("data-mgmt bars first page cache miss key={}", cacheKey);
            return loader.get();
        });
    }

    /** K 线增删改后失效概览、交易日与明细第一页缓存 */
    public void invalidateKlineDataCaches() {
        overviewCache.invalidateAll();
        tradeDatesFirstPageCache.invalidateAll();
        barsFirstPageCache.invalidateAll();
        log.debug("data-mgmt kline-related caches invalidated");
    }
}
