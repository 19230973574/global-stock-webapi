package com.globalstock.webapi.service;

import com.globalstock.webapi.infra.KlineDataTaskInfra;
import com.globalstock.webapi.infra.MarketDataInfra;
import com.globalstock.webapi.model.document.KlineDataTaskDocument;
import com.globalstock.webapi.model.document.KlineDocument;
import com.globalstock.webapi.model.document.StockInfoDocument;
import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.KlineCodeSummaryDTO;
import com.globalstock.webapi.model.dto.KlineHistoryBarPageDTO;
import com.globalstock.webapi.model.dto.KlineTradeDateItemDTO;
import com.globalstock.webapi.model.dto.KlineTradeDatePageDTO;
import com.globalstock.webapi.model.dto.UsDataMgmtOverviewDTO;
import com.globalstock.webapi.model.dto.UsStockItemDTO;
import com.globalstock.webapi.model.dto.UsStockPageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDataMgmtService {

    private static final String MARKET = "US";
    private static final String PERIOD = "1d";

    private final AuthService authService;
    private final MarketDataInfra marketDataInfra;
    private final KlineDataTaskInfra klineDataTaskInfra;
    private final AdminDataMgmtCache adminDataMgmtCache;

    public AdminDataMgmtService(AuthService authService,
                                MarketDataInfra marketDataInfra,
                                KlineDataTaskInfra klineDataTaskInfra,
                                AdminDataMgmtCache adminDataMgmtCache) {
        this.authService = authService;
        this.marketDataInfra = marketDataInfra;
        this.klineDataTaskInfra = klineDataTaskInfra;
        this.adminDataMgmtCache = adminDataMgmtCache;
    }

    public UsDataMgmtOverviewDTO getUsOverview(boolean refresh) {
        authService.requireAdminRole();
        return adminDataMgmtCache.getOverview(this::loadUsOverview, refresh);
    }

    public UsStockPageDTO listUsStocks(String codeKeyword, Integer page, Integer pageSize, boolean refresh) {
        authService.requireAdminRole();

        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        String keyword = normalizeStockKeyword(codeKeyword);

        if (currentPage == 1 && keyword == null) {
            return adminDataMgmtCache.getStocksFirstPage(
                    currentPageSize,
                    () -> loadUsStocksPage(keyword, currentPage, currentPageSize),
                    refresh);
        }
        return loadUsStocksPage(keyword, currentPage, currentPageSize);
    }

    public KlineHistoryBarPageDTO listKlineHistoryBars(String code, String date,
                                                       String startDate, String endDate,
                                                       Integer page, Integer pageSize, boolean refresh) {
        authService.requireAdminRole();

        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        String normalizedCode = normalizeCodeKeyword(code);
        String normalizedDate = normalizeOptionalDate(date);
        String normalizedStart = normalizeOptionalDate(startDate);
        String normalizedEnd = normalizeOptionalDate(endDate);

        if (isBarsFirstPageCacheable(currentPage, normalizedCode, normalizedDate, normalizedStart, normalizedEnd)) {
            return adminDataMgmtCache.getBarsFirstPage(
                    currentPageSize,
                    () -> loadKlineHistoryBarsPage(
                            normalizedCode, normalizedDate, normalizedStart, normalizedEnd,
                            currentPage, currentPageSize),
                    refresh);
        }
        return loadKlineHistoryBarsPage(
                normalizedCode, normalizedDate, normalizedStart, normalizedEnd,
                currentPage, currentPageSize);
    }

    public KlineTradeDatePageDTO listKlineTradeDates(String startDate, String endDate,
                                                     Integer page, Integer pageSize, boolean refresh) {
        authService.requireAdminRole();

        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        String normalizedStart = normalizeOptionalDate(startDate);
        String normalizedEnd = normalizeOptionalDate(endDate);

        KlineTradeDatePageDTO pageResult;
        if (currentPage == 1) {
            String cacheKey = AdminDataMgmtCache.tradeDatesFirstPageKey(
                    currentPageSize, normalizedStart, normalizedEnd);
            pageResult = adminDataMgmtCache.getTradeDatesFirstPage(
                    cacheKey,
                    () -> loadKlineTradeDatesPage(normalizedStart, normalizedEnd, currentPage, currentPageSize),
                    refresh);
        } else {
            pageResult = loadKlineTradeDatesPage(normalizedStart, normalizedEnd, currentPage, currentPageSize);
        }
        return finalizeTradeDatesPage(pageResult);
    }

    private KlineTradeDatePageDTO finalizeTradeDatesPage(KlineTradeDatePageDTO page) {
        KlineTradeDatePageDTO copy = copyTradeDatePage(page);
        enrichTradeDatesSyncStatus(copy.getItems());
        return copy;
    }

    private void enrichTradeDatesSyncStatus(List<KlineTradeDateItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<KlineDataTaskDocument> tasks = klineDataTaskInfra.findActiveTasks();
        if (tasks.isEmpty()) {
            return;
        }
        for (KlineTradeDateItemDTO item : items) {
            KlineDataTaskDocument matched = findBestTaskForDate(tasks, item.getDate());
            if (matched == null) {
                continue;
            }
            item.setSyncStatus(matched.getStatus());
            item.setSyncTaskStartDate(matched.getStartDate());
            item.setSyncTaskEndDate(matched.getEndDate());
            item.setSyncStartedAt(matched.getStartedAt());
            item.setSyncFinishedAt(matched.getFinishedAt());
        }
    }

    private KlineDataTaskDocument findBestTaskForDate(List<KlineDataTaskDocument> tasks, String date) {
        if (date == null) {
            return null;
        }
        KlineDataTaskDocument pending = null;
        for (KlineDataTaskDocument task : tasks) {
            if (!dateInRange(date, task.getStartDate(), task.getEndDate())) {
                continue;
            }
            if ("RUNNING".equals(task.getStatus())) {
                return task;
            }
            if ("PENDING".equals(task.getStatus()) && pending == null) {
                pending = task;
            }
        }
        return pending;
    }

    private boolean dateInRange(String date, String start, String end) {
        if (start == null || end == null) {
            return false;
        }
        return start.compareTo(date) <= 0 && end.compareTo(date) >= 0;
    }

    private KlineTradeDatePageDTO copyTradeDatePage(KlineTradeDatePageDTO source) {
        KlineTradeDatePageDTO copy = new KlineTradeDatePageDTO();
        copy.setPage(source.getPage());
        copy.setPageSize(source.getPageSize());
        copy.setTotal(source.getTotal());
        copy.setItems(source.getItems().stream()
                .map(item -> new KlineTradeDateItemDTO(item.getDate(), item.getBarCount()))
                .collect(Collectors.toList()));
        return copy;
    }

    private boolean isBarsFirstPageCacheable(int page, String code, String date, String start, String end) {
        return page == 1 && code == null && date == null && start == null && end == null;
    }

    private UsDataMgmtOverviewDTO loadUsOverview() {
        UsDataMgmtOverviewDTO overview = new UsDataMgmtOverviewDTO();
        overview.setMarket(MARKET);
        overview.setStockCount(marketDataInfra.countAllStockInfos(MARKET));
        overview.setHistoryBarCount(marketDataInfra.countHistoryBars(MARKET, PERIOD));
        overview.setLatestTradeDate(marketDataInfra.findLatestMarketTradeDate(MARKET, PERIOD));
        overview.setDateDistribution(marketDataInfra.findKlineLatestDateDistribution(MARKET, PERIOD, 12));

        List<String> klineCodes = marketDataInfra.findHistoryCodes(MARKET, PERIOD);
        overview.setKlineCodeCount(klineCodes.size());
        return overview;
    }

    private UsStockPageDTO loadUsStocksPage(String keyword, int currentPage, int currentPageSize) {
        long total = marketDataInfra.countStockInfos(MARKET, keyword);
        List<StockInfoDocument> stocks = marketDataInfra.findStockInfosPage(
                MARKET, keyword, currentPage, currentPageSize);

        List<String> codes = stocks.stream().map(StockInfoDocument::getCode).toList();
        Map<String, KlineCodeSummaryDTO> klineSummary = marketDataInfra.findKlineSummaryByCodes(
                MARKET, PERIOD, codes);

        List<UsStockItemDTO> items = stocks.stream()
                .map(stock -> toStockItem(stock, klineSummary.get(stock.getCode())))
                .collect(Collectors.toList());

        UsStockPageDTO result = new UsStockPageDTO();
        result.setPage(currentPage);
        result.setPageSize(currentPageSize);
        result.setTotal(total);
        result.setItems(items);
        return result;
    }

    private KlineTradeDatePageDTO loadKlineTradeDatesPage(String normalizedStart, String normalizedEnd,
                                                          int currentPage, int currentPageSize) {
        long total = marketDataInfra.countDistinctTradeDates(MARKET, PERIOD, normalizedStart, normalizedEnd);
        List<KlineTradeDateItemDTO> items = marketDataInfra.findTradeDatesPage(
                MARKET, PERIOD, normalizedStart, normalizedEnd, currentPage, currentPageSize);

        KlineTradeDatePageDTO result = new KlineTradeDatePageDTO();
        result.setPage(currentPage);
        result.setPageSize(currentPageSize);
        result.setTotal(total);
        result.setItems(items);
        return result;
    }

    private KlineHistoryBarPageDTO loadKlineHistoryBarsPage(String normalizedCode, String normalizedDate,
                                                            String normalizedStart, String normalizedEnd,
                                                            int currentPage, int currentPageSize) {
        long total = marketDataInfra.countHistoryBarsByFilter(
                MARKET, PERIOD, normalizedCode, normalizedDate, normalizedStart, normalizedEnd);
        List<KlineDocument> documents = marketDataInfra.findHistoryBarsPage(
                MARKET, PERIOD, normalizedCode, normalizedDate, normalizedStart, normalizedEnd,
                currentPage, currentPageSize);

        KlineHistoryBarPageDTO result = new KlineHistoryBarPageDTO();
        result.setPage(currentPage);
        result.setPageSize(currentPageSize);
        result.setTotal(total);
        result.setItems(documents.stream().map(this::toKlineBar).toList());
        return result;
    }

    private KlineBarDTO toKlineBar(KlineDocument document) {
        return new KlineBarDTO(
                document.getCode(),
                document.getMarket(),
                document.getPeriod(),
                document.getDate(),
                document.getOpen(),
                document.getHigh(),
                document.getLow(),
                document.getClose(),
                document.getVolume(),
                document.getTurnover(),
                document.getChange(),
                document.getChangePct());
    }

    private String normalizeStockKeyword(String codeKeyword) {
        if (codeKeyword == null || codeKeyword.isBlank()) {
            return null;
        }
        return codeKeyword.trim();
    }

    private String normalizeCodeKeyword(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        String normalized = code.trim().toUpperCase();
        if (normalized.endsWith(".US")) {
            normalized = normalized.substring(0, normalized.length() - 3);
        }
        return normalized;
    }

    private String normalizeOptionalDate(String date) {
        return date == null || date.isBlank() ? null : date.trim();
    }

    private UsStockItemDTO toStockItem(StockInfoDocument stock, KlineCodeSummaryDTO summary) {
        UsStockItemDTO item = new UsStockItemDTO();
        item.setCode(stock.getCode());
        item.setSymbol(stock.getSymbol());
        item.setName(stock.getName());
        item.setMarketCap(resolveMarketCap(stock, summary));
        if (summary != null) {
            item.setKlineLatestDate(summary.getLatestDate());
            item.setKlineBarCount(summary.getBarCount());
        }
        return item;
    }

    private Double resolveMarketCap(StockInfoDocument stock, KlineCodeSummaryDTO summary) {
        if (stock.getMarketCap() != null && stock.getMarketCap() > 0) {
            return stock.getMarketCap();
        }
        if (summary == null || summary.getClose() == null || summary.getClose() <= 0) {
            return null;
        }
        Long shares = stock.getCirculatingShares() != null && stock.getCirculatingShares() > 0
                ? stock.getCirculatingShares()
                : stock.getTotalShares();
        if (shares == null || shares <= 0) {
            return null;
        }
        return summary.getClose() * shares;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
