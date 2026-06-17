package com.globalstock.webapi.service.domain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.globalstock.webapi.infra.MarketDataInfra;
import com.globalstock.webapi.model.document.KlineDocument;
import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.QuoteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 行情领域服务。
 *
 * <p>职责说明：封装行情查询缓存、领域转换与数据不存在规则。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Service
public class MarketDataDomainService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataDomainService.class);

    private final MarketDataInfra marketDataInfra;

    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(5))
            .maximumSize(10_000)
            .build();

    public MarketDataDomainService(MarketDataInfra marketDataInfra) {
        this.marketDataInfra = marketDataInfra;
    }

    /**
     * 查询实时行情。
     *
     * @param market 市场
     * @param code 股票代码
     * @return 实时行情 DTO
     */
    public QuoteDTO getQuote(String market, String code) {
        String cacheKey = "quote:" + market + ":" + code;
        QuoteDTO cached = (QuoteDTO) localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("quote local cache hit key={}", cacheKey);
            return cached;
        }

        KlineDocument latest = marketDataInfra.findLatestTodayBar(market, code, "1m");
        if (latest == null) {
            latest = marketDataInfra.findLatestHistoryBar(market, code, "1d");
        }
        if (latest == null) {
            log.warn("quote data not found market={}, code={}", market, code);
            return null;
        }

        QuoteDTO quote = toQuote(latest);
        localCache.put(cacheKey, quote);
        return quote;
    }

    /**
     * 查询实时分时。
     *
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @param limit 返回条数
     * @return 分时 K线列表
     */
    public List<KlineBarDTO> getIntradayBars(String market, String code, String period, int limit) {
        String cacheKey = "intraday:" + market + ":" + code + ":" + period + ":" + limit;
        List<KlineBarDTO> cached = (List<KlineBarDTO>) localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("intraday local cache hit key={}", cacheKey);
            return cached;
        }

        List<KlineBarDTO> bars = marketDataInfra.findIntradayBars(market, code, period, limit)
                .stream()
                .map(this::toKlineBar)
                .toList();
        if (bars.isEmpty()) {
            log.warn("intraday bars not found market={}, code={}, period={}", market, code, period);
            return List.of();
        }

        localCache.put(cacheKey, bars);
        return bars;
    }

    /**
     * 查询历史 K线。
     *
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @param limit 返回条数
     * @return 历史 K线列表
     */
    public List<KlineBarDTO> getHistoryBars(String market, String code, String period, int limit) {
        String cacheKey = "history:" + market + ":" + code + ":" + period + ":" + limit;
        List<KlineBarDTO> cached = (List<KlineBarDTO>) localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("history local cache hit key={}", cacheKey);
            return cached;
        }

        List<KlineBarDTO> bars = marketDataInfra.findHistoryBars(market, code, period, limit)
                .stream()
                .map(this::toKlineBar)
                .toList();
        if (bars.isEmpty()) {
            log.warn("history bars not found market={}, code={}, period={}", market, code, period);
            return List.of();
        }

        localCache.put(cacheKey, bars);
        return bars;
    }

    private QuoteDTO toQuote(KlineDocument document) {
        return new QuoteDTO(
                document.getCode(),
                document.getMarket(),
                document.getClose(),
                document.getOpen(),
                document.getHigh(),
                document.getLow(),
                document.getVolume(),
                document.getTurnover(),
                document.getChange(),
                document.getChangePct(),
                document.getDate()
        );
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
}
