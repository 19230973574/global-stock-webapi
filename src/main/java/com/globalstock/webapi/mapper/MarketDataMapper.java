package com.globalstock.webapi.mapper;

import com.globalstock.webapi.model.document.KlineDocument;
import com.globalstock.webapi.model.document.StockInfoDocument;
import com.globalstock.webapi.model.dto.KlineCodeSummaryDTO;
import com.globalstock.webapi.model.dto.KlineTradeDateItemDTO;
import com.globalstock.webapi.model.dto.UsDataMgmtOverviewDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 行情 MongoDB Mapper。
 *
 * <p>职责说明：执行 MongoDB 查询，不包含业务判断。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Component
public class MarketDataMapper {

    private static final Logger log = LoggerFactory.getLogger(MarketDataMapper.class);

    private final MongoTemplate mongoTemplate;

    public MarketDataMapper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 查询 K线数据。
     *
     * @param collectionName 集合名
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @param limit 返回条数
     * @return K线文档列表
     */
    public List<KlineDocument> findKlines(String collectionName, String market, String code, String period, int limit) {
        log.debug("find klines collection={}, market={}, code={}, period={}, limit={}",
                collectionName, market, code, period, limit);
        Query query = Query.query(Criteria.where("market").is(market)
                        .and("code").is(code)
                        .and("period").is(period))
                .with(Sort.by(Sort.Direction.DESC, "date"))
                .limit(limit);
        return mongoTemplate.find(query, KlineDocument.class, collectionName);
    }

    /**
     * 查询最新 K线数据。
     *
     * @param collectionName 集合名
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @return 最新 K线文档
     */
    public KlineDocument findLatestKline(String collectionName, String market, String code, String period) {
        log.debug("find latest kline collection={}, market={}, code={}, period={}",
                collectionName, market, code, period);
        Query query = Query.query(Criteria.where("market").is(market)
                        .and("code").is(code)
                        .and("period").is(period))
                .with(Sort.by(Sort.Direction.DESC, "date"))
                .limit(1);
        return mongoTemplate.findOne(query, KlineDocument.class, collectionName);
    }

    public List<String> findDistinctCodes(String collectionName, String market, String period) {
        Query query = Query.query(Criteria.where("market").is(market).and("period").is(period));
        return mongoTemplate.findDistinct(query, "code", collectionName, String.class);
    }

    public long countKlines(String collectionName, String market, String period) {
        Query query = Query.query(Criteria.where("market").is(market).and("period").is(period));
        return mongoTemplate.count(query, KlineDocument.class, collectionName);
    }

    public String findLatestMarketDate(String collectionName, String market, String period) {
        Query query = Query.query(Criteria.where("market").is(market).and("period").is(period))
                .with(Sort.by(Sort.Direction.DESC, "date"))
                .limit(1);
        KlineDocument document = mongoTemplate.findOne(query, KlineDocument.class, collectionName);
        return document == null ? null : document.getDate();
    }

    public List<StockInfoDocument> findStockInfos(String collectionName, String market) {
        Query query = Query.query(Criteria.where("market").is(market));
        return mongoTemplate.find(query, StockInfoDocument.class, collectionName);
    }

    public long countStockInfos(String collectionName, String market, String codeKeyword) {
        return mongoTemplate.count(buildStockInfoQuery(market, codeKeyword), StockInfoDocument.class, collectionName);
    }

    public List<StockInfoDocument> findStockInfosPage(String collectionName, String market,
                                                      String codeKeyword, int page, int pageSize) {
        Query query = buildStockInfoQuery(market, codeKeyword)
                .with(Sort.by(Sort.Direction.ASC, "code"))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize);
        return mongoTemplate.find(query, StockInfoDocument.class, collectionName);
    }

    public long countStockInfos(String collectionName, String market) {
        Query query = Query.query(Criteria.where("market").is(market));
        return mongoTemplate.count(query, StockInfoDocument.class, collectionName);
    }

    public Map<String, KlineCodeSummaryDTO> findKlineSummaryByCodes(String collectionName, String market,
                                                                    String period, List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("market").is(market)
                        .and("period").is(period)
                        .and("code").in(codes)),
                Aggregation.sort(Sort.by(
                        Sort.Order.asc("code"),
                        Sort.Order.desc("date"))),
                Aggregation.group("code")
                        .first("date").as("latestDate")
                        .first("close").as("close")
                        .count().as("barCount")
        );
        AggregationResults<KlineCodeSummaryDTO> results = mongoTemplate.aggregate(
                aggregation, collectionName, KlineCodeSummaryDTO.class);
        Map<String, KlineCodeSummaryDTO> map = new HashMap<>();
        for (KlineCodeSummaryDTO item : results.getMappedResults()) {
            if (item.getCode() != null) {
                map.put(item.getCode(), item);
            }
        }
        return map;
    }

    public List<UsDataMgmtOverviewDTO.KlineDateDistributionItemDTO> findKlineLatestDateDistribution(
            String collectionName, String market, String period, int topN) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("market").is(market).and("period").is(period)),
                Aggregation.group("code").max("date").as("latest"),
                Aggregation.group("latest").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "_id"),
                Aggregation.limit(topN)
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, collectionName, Map.class);
        return results.getMappedResults().stream()
                .map(row -> new UsDataMgmtOverviewDTO.KlineDateDistributionItemDTO(
                        row.get("_id") == null ? null : String.valueOf(row.get("_id")),
                        row.get("count") == null ? 0L : ((Number) row.get("count")).longValue()))
                .toList();
    }

    private Query buildStockInfoQuery(String market, String codeKeyword) {
        Criteria criteria = Criteria.where("market").is(market);
        if (codeKeyword != null && !codeKeyword.isBlank()) {
            String keyword = codeKeyword.trim().toUpperCase();
            Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
            criteria = new Criteria().andOperator(
                    Criteria.where("market").is(market),
                    new Criteria().orOperator(
                            Criteria.where("code").regex(pattern),
                            Criteria.where("name").regex(pattern),
                            Criteria.where("symbol").regex(pattern)
                    )
            );
        }
        return Query.query(criteria);
    }

    public long countKlinesByFilter(String collectionName, String market, String period,
                                    List<String> codes, String startDate, String endDate,
                                    List<String> dates) {
        return mongoTemplate.count(buildDeleteQuery(market, period, codes, startDate, endDate, dates),
                KlineDocument.class, collectionName);
    }

    public long deleteKlinesByFilter(String collectionName, String market, String period,
                                     List<String> codes, String startDate, String endDate,
                                     List<String> dates) {
        Query query = buildDeleteQuery(market, period, codes, startDate, endDate, dates);
        return mongoTemplate.remove(query, KlineDocument.class, collectionName).getDeletedCount();
    }

    public long countHistoryBarsByFilter(String collectionName, String market, String period,
                                         String code, String date, String startDate, String endDate) {
        return mongoTemplate.count(
                buildKlineBrowseQuery(market, period, code, date, startDate, endDate),
                KlineDocument.class, collectionName);
    }

    public List<KlineDocument> findHistoryBarsPage(String collectionName, String market, String period,
                                                   String code, String date, String startDate, String endDate,
                                                   int page, int pageSize) {
        Query query = buildKlineBrowseQuery(market, period, code, date, startDate, endDate)
                .with(Sort.by(
                        Sort.Order.desc("date"),
                        Sort.Order.asc("code")))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize);
        return mongoTemplate.find(query, KlineDocument.class, collectionName);
    }

    public long countDistinctTradeDates(String collectionName, String market, String period,
                                        String startDate, String endDate) {
        Criteria match = buildTradeDateMatchCriteria(market, period, startDate, endDate);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(match),
                Aggregation.group("date"),
                Aggregation.count().as("total"));
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, collectionName, Map.class);
        List<Map> mapped = results.getMappedResults();
        if (mapped.isEmpty()) {
            return 0L;
        }
        Object total = mapped.get(0).get("total");
        return total == null ? 0L : ((Number) total).longValue();
    }

    public List<KlineTradeDateItemDTO> findTradeDatesPage(String collectionName, String market, String period,
                                                          String startDate, String endDate,
                                                          int page, int pageSize) {
        Criteria match = buildTradeDateMatchCriteria(market, period, startDate, endDate);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(match),
                Aggregation.group("date").count().as("barCount"),
                Aggregation.sort(Sort.Direction.DESC, "_id"),
                Aggregation.skip((long) (page - 1) * pageSize),
                Aggregation.limit(pageSize));
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, collectionName, Map.class);
        return results.getMappedResults().stream()
                .map(row -> new KlineTradeDateItemDTO(
                        row.get("_id") == null ? null : String.valueOf(row.get("_id")),
                        row.get("barCount") == null ? 0L : ((Number) row.get("barCount")).longValue()))
                .toList();
    }

    private Criteria buildTradeDateMatchCriteria(String market, String period, String startDate, String endDate) {
        Criteria criteria = Criteria.where("market").is(market).and("period").is(period);
        if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
            criteria = criteria.and("date").gte(startDate).lte(endDate);
        } else if (startDate != null && !startDate.isBlank()) {
            criteria = criteria.and("date").gte(startDate);
        } else if (endDate != null && !endDate.isBlank()) {
            criteria = criteria.and("date").lte(endDate);
        }
        return criteria;
    }

    private Query buildKlineBrowseQuery(String market, String period, String code,
                                        String date, String startDate, String endDate) {
        return Query.query(buildKlineBrowseCriteria(market, period, code, date, startDate, endDate));
    }

    private Criteria buildKlineBrowseCriteria(String market, String period, String code,
                                              String date, String startDate, String endDate) {
        Criteria criteria = Criteria.where("market").is(market).and("period").is(period);
        if (code != null && !code.isBlank()) {
            criteria = criteria.and("code").is(code.trim().toUpperCase());
        }
        if (date != null && !date.isBlank()) {
            criteria = criteria.and("date").is(date.trim());
        } else if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
            criteria = criteria.and("date").gte(startDate).lte(endDate);
        } else if (startDate != null && !startDate.isBlank()) {
            criteria = criteria.and("date").gte(startDate);
        } else if (endDate != null && !endDate.isBlank()) {
            criteria = criteria.and("date").lte(endDate);
        }
        return criteria;
    }

    private Query buildDeleteQuery(String market, String period, List<String> codes,
                                   String startDate, String endDate, List<String> dates) {
        Criteria criteria = Criteria.where("market").is(market).and("period").is(period);
        if (codes != null && !codes.isEmpty()) {
            criteria = criteria.and("code").in(codes);
        }
        if (dates != null && !dates.isEmpty()) {
            criteria = criteria.and("date").in(dates);
        } else if (startDate != null && endDate != null) {
            criteria = criteria.and("date").gte(startDate).lte(endDate);
        }
        return Query.query(criteria);
    }
}
