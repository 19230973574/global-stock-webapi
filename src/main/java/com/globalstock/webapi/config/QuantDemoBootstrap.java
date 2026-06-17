package com.globalstock.webapi.config;

import com.globalstock.webapi.model.document.KlineDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 量化专区演示历史 K 线：为常见美股写入约 280 个交易日数据，便于新高/新低筛选联调。
 */
@Component
@Order(21)
public class QuantDemoBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QuantDemoBootstrap.class);
    private static final String COLLECTION = "us_kline_history";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MongoTemplate mongoTemplate;

    public QuantDemoBootstrap(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        long existing = mongoTemplate.count(
                Query.query(Criteria.where("market").is("US").and("period").is("1d")),
                COLLECTION);
        if (existing >= 200) {
            return;
        }
        seedHistoryDemo();
        log.info("quant demo bootstrap completed collection={}", COLLECTION);
    }

    private void seedHistoryDemo() {
        Map<String, Double> bases = Map.of(
                "AAPL", 196.0,
                "TSLA", 182.0,
                "NVDA", 128.0,
                "MSFT", 420.0,
                "AMZN", 185.0,
                "META", 510.0,
                "GOOGL", 175.0
        );
        LocalDate end = LocalDate.now();
        bases.forEach((symbol, base) -> {
            List<KlineDocument> bars = buildHistory(symbol, base, end, 280);
            applyDemoSignals(symbol, bars);
            bars.forEach(doc -> mongoTemplate.save(doc, COLLECTION));
            log.info("seeded quant history symbol={} count={}", symbol, bars.size());
        });
    }

    private List<KlineDocument> buildHistory(String symbol, double base, LocalDate end, int days) {
        List<KlineDocument> bars = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = end.minusDays(i);
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            double drift = Math.sin(i / 8.0) * 4 + (days - i) * 0.03;
            double close = base + drift;
            KlineDocument bar = new KlineDocument();
            bar.setCode(symbol);
            bar.setMarket("US");
            bar.setPeriod("1d");
            bar.setDate(date.format(DATE));
            bar.setOpen(close - 0.4);
            bar.setHigh(close + 0.8);
            bar.setLow(close - 0.9);
            bar.setClose(close);
            bar.setVolume(8_000_000L + i * 12_000L);
            bar.setTurnover(null);
            bar.setChange(drift);
            bar.setChangePct(drift / base * 100);
            bar.setUpdatedAt(end.format(DATE));
            bars.add(bar);
        }
        return bars;
    }

    private void applyDemoSignals(String symbol, List<KlineDocument> bars) {
        if (bars.isEmpty()) {
            return;
        }
        KlineDocument latest = bars.get(bars.size() - 1);
        switch (symbol) {
            case "AAPL" -> bumpHigh(latest, maxHigh(bars, 1, 252) + 2.5);
            case "NVDA" -> bumpClose(latest, maxClose(bars, 1, 63) + 1.8);
            case "MSFT" -> bumpClose(latest, maxClose(bars, 1, 21) + 1.2);
            case "META" -> bumpHigh(latest, maxHigh(bars, 1, 5) + 0.9);
            case "AMZN" -> bumpHigh(latest, maxHigh(bars, 1, 60) + 1.5);
            case "TSLA" -> bumpLow(latest, minLow(bars, 1, 20) - 2.1);
            default -> {
            }
        }
    }

    private void bumpHigh(KlineDocument bar, double high) {
        bar.setHigh(high);
        bar.setClose(high - 0.2);
        bar.setOpen(high - 0.5);
        bar.setLow(high - 1.0);
    }

    private void bumpClose(KlineDocument bar, double close) {
        bar.setClose(close);
        bar.setHigh(close + 0.3);
        bar.setOpen(close - 0.4);
        bar.setLow(close - 0.8);
    }

    private void bumpLow(KlineDocument bar, double low) {
        bar.setLow(low);
        bar.setClose(low + 0.2);
        bar.setOpen(low + 0.5);
        bar.setHigh(low + 1.0);
    }

    private double maxHigh(List<KlineDocument> bars, int excludeFromEnd, int window) {
        int end = bars.size() - excludeFromEnd;
        int start = Math.max(0, end - window);
        return bars.subList(start, end).stream().mapToDouble(b -> b.getHigh() != null ? b.getHigh() : b.getClose()).max().orElse(0);
    }

    private double maxClose(List<KlineDocument> bars, int excludeFromEnd, int window) {
        int end = bars.size() - excludeFromEnd;
        int start = Math.max(0, end - window);
        return bars.subList(start, end).stream().mapToDouble(b -> b.getClose() != null ? b.getClose() : b.getHigh()).max().orElse(0);
    }

    private double minLow(List<KlineDocument> bars, int excludeFromEnd, int window) {
        int end = bars.size() - excludeFromEnd;
        int start = Math.max(0, end - window);
        return bars.subList(start, end).stream().mapToDouble(b -> b.getLow() != null ? b.getLow() : b.getClose()).min().orElse(0);
    }
}
