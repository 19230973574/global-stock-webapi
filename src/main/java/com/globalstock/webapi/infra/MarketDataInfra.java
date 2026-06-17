package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.MarketDataMapper;
import com.globalstock.webapi.model.document.KlineDocument;
import com.globalstock.webapi.model.document.StockInfoDocument;
import com.globalstock.webapi.model.dto.KlineCodeSummaryDTO;
import com.globalstock.webapi.model.dto.KlineTradeDateItemDTO;
import com.globalstock.webapi.model.dto.UsDataMgmtOverviewDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 行情数据访问层。
 *
 * <p>职责说明：封装 MongoDB 集合命名与 Mapper 调用。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Repository
public class MarketDataInfra {

    private static final Logger log = LoggerFactory.getLogger(MarketDataInfra.class);

    private final MarketDataMapper marketDataMapper;

    public MarketDataInfra(MarketDataMapper marketDataMapper) {
        this.marketDataMapper = marketDataMapper;
    }

    /**
     * 查询当日分时 K线。
     *
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @param limit 返回条数
     * @return K线文档列表
     */
    public List<KlineDocument> findIntradayBars(String market, String code, String period, int limit) {
        try {
            return marketDataMapper.findKlines(collectionName(market, "kline_today"), market, code, period, limit);
        } catch (Exception exception) {
            log.debug("find intraday bars failed", exception);
            throw new SystemException("查询分时数据失败", exception);
        }
    }

    /**
     * 查询历史 K线。
     *
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @param limit 返回条数
     * @return K线文档列表
     */
    public List<KlineDocument> findHistoryBars(String market, String code, String period, int limit) {
        try {
            return marketDataMapper.findKlines(collectionName(market, "kline_history"), market, code, period, limit);
        } catch (Exception exception) {
            log.debug("find history bars failed", exception);
            throw new SystemException("查询历史K线失败", exception);
        }
    }

    /**
     * 查询最新当日 K线。
     *
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @return 最新 K线文档
     */
    public KlineDocument findLatestTodayBar(String market, String code, String period) {
        try {
            return marketDataMapper.findLatestKline(collectionName(market, "kline_today"), market, code, period);
        } catch (Exception exception) {
            log.debug("find latest today bar failed", exception);
            throw new SystemException("查询实时行情失败", exception);
        }
    }

    /**
     * 查询最新历史 K线。
     *
     * @param market 市场
     * @param code 股票代码
     * @param period 周期
     * @return 最新 K线文档
     */
    public KlineDocument findLatestHistoryBar(String market, String code, String period) {
        try {
            return marketDataMapper.findLatestKline(collectionName(market, "kline_history"), market, code, period);
        } catch (Exception exception) {
            log.debug("find latest history bar failed", exception);
            throw new SystemException("查询历史行情失败", exception);
        }
    }

    public List<String> findHistoryCodes(String market, String period) {
        try {
            return marketDataMapper.findDistinctCodes(collectionName(market, "kline_history"), market, period);
        } catch (Exception exception) {
            log.debug("find history codes failed", exception);
            throw new SystemException("查询股票列表失败", exception);
        }
    }

    public String findLatestMarketTradeDate(String market, String period) {
        try {
            return marketDataMapper.findLatestMarketDate(collectionName(market, "kline_history"), market, period);
        } catch (Exception exception) {
            log.debug("find latest market trade date failed", exception);
            throw new SystemException("查询最近交易日失败", exception);
        }
    }

    public long countHistoryBars(String market, String period) {
        try {
            return marketDataMapper.countKlines(collectionName(market, "kline_history"), market, period);
        } catch (Exception exception) {
            log.debug("count history bars failed", exception);
            throw new SystemException("统计历史K线失败", exception);
        }
    }

    public List<StockInfoDocument> findStockInfos(String market) {
        try {
            return marketDataMapper.findStockInfos(collectionName(market, "stock_info"), market);
        } catch (Exception exception) {
            log.debug("find stock infos failed", exception);
            throw new SystemException("查询股票基础信息失败", exception);
        }
    }

    public long countHistoryBarsForDelete(String market, String period, List<String> codes,
                                          String startDate, String endDate, List<String> dates) {
        try {
            return marketDataMapper.countKlinesByFilter(
                    collectionName(market, "kline_history"), market, period, codes,
                    startDate, endDate, dates);
        } catch (Exception exception) {
            log.debug("count history bars for delete failed", exception);
            throw new SystemException("统计待删除 K 线失败", exception);
        }
    }

    public long deleteHistoryBars(String market, String period, List<String> codes,
                                  String startDate, String endDate, List<String> dates) {
        try {
            return marketDataMapper.deleteKlinesByFilter(
                    collectionName(market, "kline_history"), market, period, codes,
                    startDate, endDate, dates);
        } catch (Exception exception) {
            log.debug("delete history bars failed", exception);
            throw new SystemException("删除历史 K 线失败", exception);
        }
    }

    public long countStockInfos(String market, String codeKeyword) {
        try {
            return marketDataMapper.countStockInfos(collectionName(market, "stock_info"), market, codeKeyword);
        } catch (Exception exception) {
            log.debug("count stock infos failed", exception);
            throw new SystemException("统计股票数量失败", exception);
        }
    }

    public long countAllStockInfos(String market) {
        try {
            return marketDataMapper.countStockInfos(collectionName(market, "stock_info"), market);
        } catch (Exception exception) {
            log.debug("count all stock infos failed", exception);
            throw new SystemException("统计股票数量失败", exception);
        }
    }

    public List<StockInfoDocument> findStockInfosPage(String market, String codeKeyword, int page, int pageSize) {
        try {
            return marketDataMapper.findStockInfosPage(
                    collectionName(market, "stock_info"), market, codeKeyword, page, pageSize);
        } catch (Exception exception) {
            log.debug("find stock infos page failed", exception);
            throw new SystemException("查询股票列表失败", exception);
        }
    }

    public Map<String, KlineCodeSummaryDTO> findKlineSummaryByCodes(String market, String period, List<String> codes) {
        try {
            return marketDataMapper.findKlineSummaryByCodes(
                    collectionName(market, "kline_history"), market, period, codes);
        } catch (Exception exception) {
            log.debug("find kline summary by codes failed", exception);
            throw new SystemException("查询 K 线摘要失败", exception);
        }
    }

    public List<UsDataMgmtOverviewDTO.KlineDateDistributionItemDTO> findKlineLatestDateDistribution(
            String market, String period, int topN) {
        try {
            return marketDataMapper.findKlineLatestDateDistribution(
                    collectionName(market, "kline_history"), market, period, topN);
        } catch (Exception exception) {
            log.debug("find kline date distribution failed", exception);
            throw new SystemException("查询 K 线日期分布失败", exception);
        }
    }

    public long countHistoryBarsByFilter(String market, String period, String code,
                                         String date, String startDate, String endDate) {
        try {
            return marketDataMapper.countHistoryBarsByFilter(
                    collectionName(market, "kline_history"), market, period,
                    code, date, startDate, endDate);
        } catch (Exception exception) {
            log.debug("count history bars by filter failed", exception);
            throw new SystemException("统计 K 线条数失败", exception);
        }
    }

    public List<KlineDocument> findHistoryBarsPage(String market, String period, String code,
                                                   String date, String startDate, String endDate,
                                                   int page, int pageSize) {
        try {
            return marketDataMapper.findHistoryBarsPage(
                    collectionName(market, "kline_history"), market, period,
                    code, date, startDate, endDate, page, pageSize);
        } catch (Exception exception) {
            log.debug("find history bars page failed", exception);
            throw new SystemException("查询 K 线列表失败", exception);
        }
    }

    public long countDistinctTradeDates(String market, String period, String startDate, String endDate) {
        try {
            return marketDataMapper.countDistinctTradeDates(
                    collectionName(market, "kline_history"), market, period, startDate, endDate);
        } catch (Exception exception) {
            log.debug("count distinct trade dates failed", exception);
            throw new SystemException("统计交易日数量失败", exception);
        }
    }

    public List<KlineTradeDateItemDTO> findTradeDatesPage(String market, String period,
                                                           String startDate, String endDate,
                                                           int page, int pageSize) {
        try {
            return marketDataMapper.findTradeDatesPage(
                    collectionName(market, "kline_history"), market, period,
                    startDate, endDate, page, pageSize);
        } catch (Exception exception) {
            log.debug("find trade dates page failed", exception);
            throw new SystemException("查询交易日列表失败", exception);
        }
    }

    private String collectionName(String market, String dataType) {
        return market.toLowerCase(Locale.ROOT) + "_" + dataType;
    }
}
