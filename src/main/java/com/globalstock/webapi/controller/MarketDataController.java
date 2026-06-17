package com.globalstock.webapi.controller;

import com.globalstock.webapi.common.ApiResponse;
import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.QuoteDTO;
import com.globalstock.webapi.model.request.MarketDataQuery;
import com.globalstock.webapi.service.MarketDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行情开放接口。
 *
 * <p>职责说明：适配 HTTP 请求并委托业务 Service。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@RestController
@RequestMapping("/market-data/v1")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * 获取实时行情。
     *
     * @param code 股票代码
     * @param market 市场
     * @return 实时行情
     */
    @GetMapping("/quotes/{code}")
    public ApiResponse<QuoteDTO> getQuote(@PathVariable String code,
                                          @RequestParam(defaultValue = "US") String market,
                                          @RequestHeader("X-API-Token") String apiToken) {
        return ApiResponse.success(marketDataService.getQuote(new MarketDataQuery(market, code, null, null, apiToken)));
    }

    /**
     * 获取实时分时。
     *
     * @param code 股票代码
     * @param market 市场
     * @param period 周期
     * @param limit 返回条数
     * @return 分时 K线列表
     */
    @GetMapping("/intraday-bars")
    public ApiResponse<List<KlineBarDTO>> getIntradayBars(@RequestParam String code,
                                                          @RequestParam(defaultValue = "US") String market,
                                                          @RequestParam(defaultValue = "1m") String period,
                                                          @RequestParam(defaultValue = "240") Integer limit,
                                                          @RequestHeader("X-API-Token") String apiToken) {
        return ApiResponse.success(marketDataService.getIntradayBars(new MarketDataQuery(market, code, period, limit, apiToken)));
    }

    /**
     * 获取历史 K线。
     *
     * @param code 股票代码
     * @param market 市场
     * @param period 周期
     * @param limit 返回条数
     * @return 历史 K线列表
     */
    @GetMapping("/history-bars")
    public ApiResponse<List<KlineBarDTO>> getHistoryBars(@RequestParam String code,
                                                         @RequestParam(defaultValue = "US") String market,
                                                         @RequestParam(defaultValue = "1d") String period,
                                                         @RequestParam(defaultValue = "100") Integer limit,
                                                         @RequestHeader("X-API-Token") String apiToken) {
        return ApiResponse.success(marketDataService.getHistoryBars(new MarketDataQuery(market, code, period, limit, apiToken)));
    }
}
