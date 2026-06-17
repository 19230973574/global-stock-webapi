package com.globalstock.webapi.config;

import com.globalstock.webapi.mapper.MarketDataMapper;
import com.globalstock.webapi.model.document.KlineDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 开发环境演示行情：当分时库为空时，为常见标的写入少量 1m K 线，便于接口预览与用量联调。
 */
@Component
@Order(20)
public class MarketDataDemoBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MarketDataDemoBootstrap.class);
    private static final String COLLECTION = "us_kline_today";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final MongoTemplate mongoTemplate;
    private final MarketDataMapper marketDataMapper;

    public MarketDataDemoBootstrap(MongoTemplate mongoTemplate, MarketDataMapper marketDataMapper) {
        this.mongoTemplate = mongoTemplate;
        this.marketDataMapper = marketDataMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        long existing = mongoTemplate.count(new Query(), COLLECTION);
        if (existing > 0) {
            return;
        }
        seedUsIntradayDemo();
        log.info("market data demo bootstrap completed collection={}", COLLECTION);
    }

    private void seedUsIntradayDemo() {
        List<String> symbols = List.of("AAPL", "TSLA", "NVDA");
        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

        for (String symbol : symbols) {
            double basePrice = resolveBasePrice(symbol);
            List<KlineDocument> bars = new ArrayList<>();
            for (int i = 59; i >= 0; i--) {
                LocalDateTime ts = base.minusMinutes(i);
                double drift = (59 - i) * 0.02;
                double close = basePrice + drift;
                KlineDocument bar = new KlineDocument();
                bar.setCode(symbol);
                bar.setMarket("US");
                bar.setPeriod("1m");
                bar.setDate(ts.format(TS));
                bar.setOpen(close - 0.05);
                bar.setHigh(close + 0.12);
                bar.setLow(close - 0.10);
                bar.setClose(close);
                bar.setVolume(50_000L + i * 800L);
                bar.setTurnover(null);
                bar.setChange(drift);
                bar.setChangePct(drift / basePrice * 100);
                bar.setUpdatedAt(LocalDateTime.now().format(TS));
                bars.add(bar);
            }
            bars.forEach(doc -> mongoTemplate.save(doc, COLLECTION));
            log.info("seeded demo intraday bars symbol={} count={} basePrice={}", symbol, bars.size(), basePrice);
        }
    }

    private double resolveBasePrice(String symbol) {
        KlineDocument history = marketDataMapper.findLatestKline("us_kline_history", "US", symbol, "1d");
        if (history != null && history.getClose() != null) {
            return history.getClose();
        }
        return switch (symbol) {
            case "TSLA" -> 182.0;
            case "NVDA" -> 128.0;
            default -> 196.0;
        };
    }
}
