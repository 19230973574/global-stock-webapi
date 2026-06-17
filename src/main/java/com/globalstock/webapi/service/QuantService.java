package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.context.AuthContext;
import com.globalstock.webapi.model.document.KlineDocument;
import com.globalstock.webapi.model.document.StockInfoDocument;
import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.QuantDataOverviewDTO;
import com.globalstock.webapi.model.dto.QuantScreenDTO;
import com.globalstock.webapi.model.dto.QuantScreenPageDTO;
import com.globalstock.webapi.model.dto.QuantSignalDTO;
import com.globalstock.webapi.infra.MarketDataInfra;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class QuantService {

    private static final Logger log = LoggerFactory.getLogger(QuantService.class);
    private static final String QUANT_PERMISSION = "us_quant_zone";
    private static final String MARKET = "US";
    private static final String PERIOD = "1d";
    private static final double MIN_CLOSE_PRICE = 1.0;
    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_BAR_WINDOW = 253;
    private static final List<Integer> DEFAULT_NEW_HIGH_LOW_WINDOWS = List.of(5, 10, 20, 60);
    private static final Map<String, Integer> PERIOD_WINDOWS = Map.of(
            "1m", 21,
            "3m", 63,
            "6m", 126,
            "1y", 252
    );
    private static final Map<String, String> PERIOD_LABELS = Map.of(
            "1m", "一月新高",
            "3m", "三月新高",
            "6m", "半年新高",
            "1y", "一年新高"
    );

    private final MarketDataInfra marketDataInfra;
    private final SubscriptionDomainService subscriptionDomainService;

    /** 全市场扫描快照：同一交易日 + 代码筛选条件下，所有窗口/周期只算一次 */
    private final Cache<String, QuantScanSnapshot> snapshotCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(32)
            .build();

    /** 单标的日 K 缓存，供快照构建与 K 线预览复用 */
    private final Cache<String, List<KlineDocument>> barCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(20_000)
            .build();

    /** 概览统计缓存 */
    private final Cache<String, QuantDataOverviewDTO> overviewCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(4)
            .build();

    /** K 线预览 DTO 缓存 */
    private final Cache<String, List<KlineBarDTO>> klineDtoCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(5_000)
            .build();

    /** 最近交易日 + 全量代码列表 */
    private final Cache<String, MarketMeta> marketMetaCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(4)
            .build();

    /** 股本 + 名称信息 */
    private final Cache<String, Map<String, StockProfile>> stockProfileCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(4)
            .build();

    public QuantService(MarketDataInfra marketDataInfra,
                        SubscriptionDomainService subscriptionDomainService) {
        this.marketDataInfra = marketDataInfra;
        this.subscriptionDomainService = subscriptionDomainService;
    }

    public QuantScreenDTO screenUsMarket(Integer days, Integer limit) {
        assertQuantZoneAccess();
        return screenUsMarketData(days, limit, null, null, null);
    }

    public QuantScreenPageDTO screenUsMarketPage(String signalType, Integer days, String period,
                                                 Integer page, Integer pageSize, String codeFilter,
                                                 Double minMarketCap, Double maxMarketCap,
                                                 String sortBy, String sortOrder,
                                                 boolean requireUserAuth) {
        if (requireUserAuth) {
            assertQuantZoneAccess();
        }
        return buildScreenPage(signalType, days, period, page, pageSize, codeFilter,
                minMarketCap, maxMarketCap, sortBy, sortOrder);
    }

    public List<KlineBarDTO> getUsHistoryBars(String code, Integer limit, boolean requireUserAuth) {
        if (requireUserAuth) {
            assertQuantZoneAccess();
        }
        if (code == null || code.isBlank()) {
            throw new BusinessException("请提供股票代码");
        }
        String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
        int barLimit = normalizeBarLimit(limit);
        MarketMeta meta = getMarketMeta();
        String cacheKey = meta.tradeDate() + "|" + normalizedCode + "|" + barLimit;
        return klineDtoCache.get(cacheKey, key -> loadKlineDto(normalizedCode, barLimit, meta.tradeDate()));
    }

    public QuantScreenPageDTO buildScreenPage(String signalType, Integer days, String period,
                                              Integer page, Integer pageSize, String codeFilter,
                                              Double minMarketCap, Double maxMarketCap,
                                              String sortBy, String sortOrder) {
        String normalizedSignalType = normalizeSignalType(signalType);
        int windowDays = normalizeWindowDays(days);
        String normalizedPeriod = normalizePeriodKey(period, normalizedSignalType);
        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        MarketCapBounds marketCapBounds = normalizeMarketCapBounds(minMarketCap, maxMarketCap);
        String normalizedSortBy = normalizeSortBy(sortBy);
        boolean sortAsc = normalizeSortAsc(sortOrder, normalizedSignalType, normalizedSortBy);

        QuantScanSnapshot snapshot = getSnapshot(codeFilter, marketCapBounds);
        List<QuantSignalDTO> allSignals = new ArrayList<>(
                snapshot.signals(normalizedSignalType, windowDays, normalizedPeriod));
        allSignals = sortSignals(allSignals, normalizedSortBy, sortAsc);
        long total = allSignals.size();
        int fromIndex = (currentPage - 1) * currentPageSize;
        List<QuantSignalDTO> items = fromIndex >= total
                ? List.of()
                : allSignals.subList(fromIndex, Math.min(fromIndex + currentPageSize, allSignals.size()));

        QuantScreenPageDTO result = new QuantScreenPageDTO();
        result.setTradeDate(snapshot.tradeDate());
        result.setSignalType(normalizedSignalType);
        result.setPeriod(normalizedPeriod);
        result.setWindowDays(windowDays);
        result.setPage(currentPage);
        result.setPageSize(currentPageSize);
        result.setTotal(total);
        result.setMinMarketCap(marketCapBounds.min());
        result.setMaxMarketCap(marketCapBounds.max());
        result.setSortBy(normalizedSortBy);
        result.setSortOrder(sortAsc ? "asc" : "desc");
        result.setItems(items);
        log.debug("quant page market={}, signalType={}, page={}, total={}, cached=true",
                MARKET, normalizedSignalType, currentPage, total);
        return result;
    }

    public QuantDataOverviewDTO getUsOverview() {
        return overviewCache.get("us", key -> {
            MarketMeta meta = getMarketMeta();
            QuantDataOverviewDTO overview = new QuantDataOverviewDTO();
            overview.setMarket(MARKET);
            overview.setCollection("us_kline_history");
            overview.setLatestTradeDate(meta.tradeDate());
            overview.setSymbolCount(meta.codes().size());
            overview.setHistoryBarCount(marketDataInfra.countHistoryBars(MARKET, PERIOD));
            overview.setSupportedWindows(DEFAULT_NEW_HIGH_LOW_WINDOWS);
            overview.setPeriodLabels(PERIOD_LABELS);
            return overview;
        });
    }

    public QuantScreenDTO screenUsMarketData(Integer days, Integer limit, String codeFilter,
                                             Double minMarketCap, Double maxMarketCap) {
        int windowDays = normalizeWindowDays(days);
        int resultLimit = normalizeLimit(limit);
        MarketCapBounds marketCapBounds = normalizeMarketCapBounds(minMarketCap, maxMarketCap);
        QuantScanSnapshot snapshot = getSnapshot(codeFilter, marketCapBounds);

        Map<String, List<QuantSignalDTO>> periodHighs = new LinkedHashMap<>();
        PERIOD_WINDOWS.keySet().forEach(periodKey ->
                periodHighs.put(periodKey, trimTop(snapshot.periodHighs().get(periodKey), resultLimit, true)));

        QuantScreenDTO result = new QuantScreenDTO();
        result.setTradeDate(snapshot.tradeDate());
        result.setNewHighs(trimTop(snapshot.newHighs().get(windowDays), resultLimit, true));
        result.setNewLows(trimTop(snapshot.newLows().get(windowDays), resultLimit, false));
        result.setPeriodHighs(periodHighs);
        return result;
    }

    public List<Integer> supportedWindows() {
        assertQuantZoneAccess();
        return DEFAULT_NEW_HIGH_LOW_WINDOWS;
    }

    private QuantScanSnapshot getSnapshot(String codeFilter, MarketCapBounds marketCapBounds) {
        MarketMeta meta = getMarketMeta();
        List<String> codes = filterCodes(meta.codes(), codeFilter);
        if (codes.isEmpty()) {
            throw new BusinessException(codeFilter == null || codeFilter.isBlank()
                    ? "暂无美股历史数据，请稍后再试"
                    : "未找到匹配代码: " + codeFilter.trim().toUpperCase(Locale.ROOT));
        }
        String cacheKey = snapshotCacheKey(meta.tradeDate(), codeFilter, marketCapBounds);
        return snapshotCache.get(cacheKey, key -> buildSnapshot(meta.tradeDate(), codes, marketCapBounds));
    }

    private MarketMeta getMarketMeta() {
        return marketMetaCache.get("us", key -> {
            String tradeDate = marketDataInfra.findLatestMarketTradeDate(MARKET, PERIOD);
            if (tradeDate == null || tradeDate.isBlank()) {
                throw new BusinessException("暂无美股历史数据，请稍后再试");
            }
            List<String> codes = marketDataInfra.findHistoryCodes(MARKET, PERIOD);
            if (codes.isEmpty()) {
                throw new BusinessException("暂无美股历史数据，请稍后再试");
            }
            return new MarketMeta(normalizeDate(tradeDate), codes);
        });
    }

    private QuantScanSnapshot buildSnapshot(String tradeDate, List<String> codes, MarketCapBounds marketCapBounds) {
        long start = System.currentTimeMillis();
        Map<String, StockProfile> profileMap = getStockProfileMap();
        Map<Integer, List<QuantSignalDTO>> newHighs = initWindowMap();
        Map<Integer, List<QuantSignalDTO>> newLows = initWindowMap();
        Map<String, List<QuantSignalDTO>> periodHighs = new LinkedHashMap<>();
        PERIOD_WINDOWS.keySet().forEach(period -> periodHighs.put(period, new ArrayList<>()));

        for (String code : codes) {
            List<KlineDocument> bars = getCachedBars(tradeDate, code);
            if (bars.size() < 2) {
                continue;
            }
            KlineDocument latest = bars.get(0);
            if (!isSameTradeDate(latest.getDate(), tradeDate) || !passesPriceFilter(latest)) {
                continue;
            }

            Double marketCap = resolveMarketCap(code, latest.getClose(), profileMap);
            if (!passesMarketCapFilter(marketCap, marketCapBounds)) {
                continue;
            }
            String name = resolveStockName(code, profileMap);

            for (int window : DEFAULT_NEW_HIGH_LOW_WINDOWS) {
                detectNewHigh(code, name, latest, bars, window, marketCap).ifPresent(newHighs.get(window)::add);
                detectNewLow(code, name, latest, bars, window, marketCap).ifPresent(newLows.get(window)::add);
            }
            for (Map.Entry<String, Integer> entry : PERIOD_WINDOWS.entrySet()) {
                detectPeriodHigh(code, name, latest, bars, entry.getKey(), entry.getValue(), marketCap)
                        .ifPresent(signal -> periodHighs.get(entry.getKey()).add(signal));
            }
        }

        newHighs.replaceAll((window, list) -> trimTop(list, Integer.MAX_VALUE, true));
        newLows.replaceAll((window, list) -> trimTop(list, Integer.MAX_VALUE, false));
        periodHighs.replaceAll((period, list) -> trimTop(list, Integer.MAX_VALUE, true));

        log.info("quant snapshot built tradeDate={}, codes={}, cost={}ms, newHigh20={}, newLow20={}",
                tradeDate, codes.size(), System.currentTimeMillis() - start,
                newHighs.get(20).size(), newLows.get(20).size());
        return new QuantScanSnapshot(tradeDate, newHighs, newLows, periodHighs);
    }

    private Map<Integer, List<QuantSignalDTO>> initWindowMap() {
        Map<Integer, List<QuantSignalDTO>> map = new LinkedHashMap<>();
        DEFAULT_NEW_HIGH_LOW_WINDOWS.forEach(window -> map.put(window, new ArrayList<>()));
        return map;
    }

    private List<KlineDocument> getCachedBars(String tradeDate, String code) {
        String cacheKey = tradeDate + "|" + code;
        return barCache.get(cacheKey, key ->
                marketDataInfra.findHistoryBars(MARKET, code, PERIOD, MAX_BAR_WINDOW + 1));
    }

    private List<KlineBarDTO> loadKlineDto(String code, int limit, String tradeDate) {
        List<KlineDocument> bars = getCachedBars(tradeDate, code);
        int count = Math.min(limit, bars.size());
        List<KlineBarDTO> result = new ArrayList<>(count);
        for (int i = count - 1; i >= 0; i--) {
            result.add(toKlineBar(bars.get(i)));
        }
        return result;
    }

    private String snapshotCacheKey(String tradeDate, String codeFilter, MarketCapBounds marketCapBounds) {
        String filter = codeFilter == null || codeFilter.isBlank()
                ? ""
                : codeFilter.trim().toUpperCase(Locale.ROOT);
        // v3：支持市值区间筛选
        return "v3|" + tradeDate + "|" + filter + "|" + marketCapBounds.cacheKey();
    }

    private Map<String, StockProfile> getStockProfileMap() {
        return stockProfileCache.get(MARKET, key -> {
            Map<String, StockProfile> profiles = new LinkedHashMap<>();
            for (StockInfoDocument info : marketDataInfra.findStockInfos(MARKET)) {
                if (info.getCode() == null || info.getCode().isBlank()) {
                    continue;
                }
                Long shareCount = info.getCirculatingShares() != null && info.getCirculatingShares() > 0
                        ? info.getCirculatingShares()
                        : info.getTotalShares();
                profiles.put(info.getCode().toUpperCase(Locale.ROOT),
                        new StockProfile(resolveStockName(info), shareCount));
            }
            return profiles;
        });
    }

    private String resolveStockName(String code, Map<String, StockProfile> profileMap) {
        StockProfile profile = profileMap.get(code.toUpperCase(Locale.ROOT));
        return profile == null ? null : profile.name();
    }

    private String resolveStockName(StockInfoDocument info) {
        if (info.getNameCn() != null && !info.getNameCn().isBlank()) {
            return info.getNameCn().trim();
        }
        if (info.getName() != null && !info.getName().isBlank()) {
            return info.getName().trim();
        }
        return null;
    }

    private Double resolveMarketCap(String code, Double close, Map<String, StockProfile> profileMap) {
        if (close == null || close <= 0) {
            return null;
        }
        StockProfile profile = profileMap.get(code.toUpperCase(Locale.ROOT));
        if (profile == null || profile.shares() == null || profile.shares() <= 0) {
            return null;
        }
        return close * profile.shares();
    }

    private boolean passesMarketCapFilter(Double marketCap, MarketCapBounds bounds) {
        if (!bounds.enabled()) {
            return true;
        }
        if (marketCap == null) {
            return false;
        }
        if (bounds.min() != null && marketCap < bounds.min()) {
            return false;
        }
        return bounds.max() == null || marketCap <= bounds.max();
    }

    private MarketCapBounds normalizeMarketCapBounds(Double minMarketCap, Double maxMarketCap) {
        Double min = normalizeMarketCapValue(minMarketCap);
        Double max = normalizeMarketCapValue(maxMarketCap);
        if (min != null && max != null && min > max) {
            throw new BusinessException("最小市值不能大于最大市值");
        }
        return new MarketCapBounds(min, max);
    }

    private Double normalizeMarketCapValue(Double value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private void assertQuantZoneAccess() {
        AuthContext.SessionInfo session = AuthContext.get();
        if (session == null || session.email() == null || session.email().isBlank()) {
            throw new BusinessException("请先登录");
        }
        Set<String> permissions = subscriptionDomainService.resolveActiveProductIds(session.email());
        if (!permissions.contains(QUANT_PERMISSION)) {
            throw new BusinessException("请先申请开通量化专区");
        }
    }

    private int normalizeWindowDays(Integer days) {
        if (days == null) {
            return 20;
        }
        if (days < 2 || days > 252) {
            throw new BusinessException("窗口天数需在 2-252 之间");
        }
        return days;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException("每页条数需在 1-" + MAX_PAGE_SIZE + " 之间");
        }
        return pageSize;
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizeBarLimit(Integer limit) {
        if (limit == null) {
            return 60;
        }
        if (limit < 10 || limit > 252) {
            throw new BusinessException("K线条数需在 10-252 之间");
        }
        return limit;
    }

    private String normalizeSignalType(String signalType) {
        if (signalType == null || signalType.isBlank()) {
            return "new_high";
        }
        String normalized = signalType.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("new_high", "new_low", "period_high").contains(normalized)) {
            throw new BusinessException("不支持的信号类型");
        }
        return normalized;
    }

    private String normalizePeriodKey(String period, String signalType) {
        if (!"period_high".equals(signalType)) {
            return null;
        }
        String normalized = period == null || period.isBlank() ? "1m" : period.trim().toLowerCase(Locale.ROOT);
        if (!PERIOD_WINDOWS.containsKey(normalized)) {
            throw new BusinessException("不支持的周期");
        }
        return normalized;
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
                document.getChangePct()
        );
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1 || limit > 500) {
            throw new BusinessException("返回条数需在 1-500 之间");
        }
        return limit;
    }

    private List<String> filterCodes(List<String> codes, String codeFilter) {
        if (codeFilter == null || codeFilter.isBlank()) {
            return codes;
        }
        String keyword = codeFilter.trim().toUpperCase(Locale.ROOT);
        return codes.stream()
                .filter(code -> code.toUpperCase(Locale.ROOT).contains(keyword))
                .toList();
    }

    private boolean isSameTradeDate(String barDate, String marketDate) {
        return normalizeDate(barDate).equals(normalizeDate(marketDate));
    }

    private String normalizeDate(String date) {
        if (date == null) {
            return "";
        }
        return date.length() >= 10 ? date.substring(0, 10) : date;
    }

    private boolean passesPriceFilter(KlineDocument latest) {
        Double close = latest.getClose();
        return close != null && close >= MIN_CLOSE_PRICE;
    }

    private List<QuantSignalDTO> trimTop(List<QuantSignalDTO> signals, int limit, boolean desc) {
        return sortSignals(signals, "break_pct", !desc).stream().limit(limit).toList();
    }

    private List<QuantSignalDTO> sortSignals(List<QuantSignalDTO> signals, String sortBy, boolean asc) {
        Comparator<Double> doubleComparator = asc ? Comparator.naturalOrder() : Comparator.reverseOrder();
        Comparator<QuantSignalDTO> comparator = switch (sortBy) {
            case "close" -> Comparator.comparing(
                    QuantSignalDTO::getClose, Comparator.nullsLast(doubleComparator));
            default -> Comparator.comparing(
                    QuantSignalDTO::getBreakPct, Comparator.nullsLast(doubleComparator));
        };
        return signals.stream().sorted(comparator).toList();
    }

    private String normalizeSortBy(String sortBy) {
        if (sortBy != null && "close".equalsIgnoreCase(sortBy.trim())) {
            return "close";
        }
        return "break_pct";
    }

    private boolean normalizeSortAsc(String sortOrder, String signalType, String sortBy) {
        if (sortOrder != null && !sortOrder.isBlank()) {
            return "asc".equalsIgnoreCase(sortOrder.trim());
        }
        if ("close".equals(sortBy)) {
            return false;
        }
        return "new_low".equals(signalType);
    }

    private Optional<QuantSignalDTO> detectNewHigh(String code, String name, KlineDocument latest,
                                                   List<KlineDocument> bars, int windowDays,
                                                   Double marketCap) {
        List<KlineDocument> prior = priorBars(bars, windowDays);
        if (prior.size() < windowDays) {
            return Optional.empty();
        }
        Double latestClose = latest.getClose();
        if (latestClose == null || latestClose < MIN_CLOSE_PRICE) {
            return Optional.empty();
        }
        double priorMaxClose = prior.stream()
                .map(KlineDocument::getClose)
                .filter(close -> close != null && close >= MIN_CLOSE_PRICE)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);
        if (priorMaxClose < MIN_CLOSE_PRICE || latestClose <= priorMaxClose) {
            return Optional.empty();
        }
        return Optional.of(buildSignal(code, name, latest, "new_high", windowDays, null, priorMaxClose, latestClose, marketCap));
    }

    private Optional<QuantSignalDTO> detectNewLow(String code, String name, KlineDocument latest,
                                                  List<KlineDocument> bars, int windowDays,
                                                  Double marketCap) {
        List<KlineDocument> prior = priorBars(bars, windowDays);
        if (prior.size() < windowDays) {
            return Optional.empty();
        }
        Double latestClose = latest.getClose();
        if (latestClose == null || latestClose < MIN_CLOSE_PRICE) {
            return Optional.empty();
        }
        double priorMinClose = prior.stream()
                .map(KlineDocument::getClose)
                .filter(close -> close != null && close > 0)
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0);
        if (priorMinClose <= 0 || latestClose >= priorMinClose) {
            return Optional.empty();
        }
        return Optional.of(buildSignal(code, name, latest, "new_low", windowDays, null, priorMinClose, latestClose, marketCap));
    }

    private Optional<QuantSignalDTO> detectPeriodHigh(String code, String name, KlineDocument latest,
                                                      List<KlineDocument> bars, String period, int windowDays,
                                                      Double marketCap) {
        List<KlineDocument> prior = priorBars(bars, windowDays);
        if (prior.size() < windowDays) {
            return Optional.empty();
        }
        Double latestClose = latest.getClose();
        if (latestClose == null || latestClose < MIN_CLOSE_PRICE) {
            return Optional.empty();
        }
        double priorMaxClose = prior.stream()
                .map(KlineDocument::getClose)
                .filter(close -> close != null && close >= MIN_CLOSE_PRICE)
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);
        if (priorMaxClose < MIN_CLOSE_PRICE || latestClose <= priorMaxClose) {
            return Optional.empty();
        }
        return Optional.of(buildSignal(code, name, latest, "period_high", windowDays, period, priorMaxClose, latestClose, marketCap));
    }

    private List<KlineDocument> priorBars(List<KlineDocument> bars, int windowDays) {
        int end = Math.min(windowDays + 1, bars.size());
        if (end <= 1) {
            return List.of();
        }
        return bars.subList(1, end);
    }

    private QuantSignalDTO buildSignal(String code, String name, KlineDocument latest, String signalType,
                                       int windowDays, String period, double priorExtreme,
                                       double currentValue, Double marketCap) {
        QuantSignalDTO signal = new QuantSignalDTO();
        signal.setCode(code);
        signal.setName(name);
        signal.setMarket(MARKET);
        signal.setTradeDate(normalizeDate(latest.getDate()));
        signal.setClose(latest.getClose());
        signal.setHigh(latest.getHigh());
        signal.setLow(latest.getLow());
        signal.setWindowDays(windowDays);
        signal.setPeriod(period);
        signal.setSignalType(signalType);
        signal.setPriorExtreme(round(priorExtreme));
        signal.setBreakPct(round((currentValue - priorExtreme) / priorExtreme * 100));
        signal.setMarketCap(marketCap == null ? null : round(marketCap));
        return signal;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record MarketMeta(String tradeDate, List<String> codes) {
    }

    private record StockProfile(String name, Long shares) {
    }

    private record MarketCapBounds(Double min, Double max) {
        boolean enabled() {
            return min != null || max != null;
        }

        String cacheKey() {
            return (min == null ? "" : min) + "-" + (max == null ? "" : max);
        }
    }

    private record QuantScanSnapshot(
            String tradeDate,
            Map<Integer, List<QuantSignalDTO>> newHighs,
            Map<Integer, List<QuantSignalDTO>> newLows,
            Map<String, List<QuantSignalDTO>> periodHighs
    ) {
        List<QuantSignalDTO> signals(String signalType, int windowDays, String period) {
            return switch (signalType) {
                case "new_low" -> newLows.getOrDefault(windowDays, List.of());
                case "period_high" -> periodHighs.getOrDefault(period, List.of());
                default -> newHighs.getOrDefault(windowDays, List.of());
            };
        }
    }
}
