package com.globalstock.webapi.model.request;

/**
 * 行情查询参数。
 *
 * <p>职责说明：承载 Controller 到 Service 的查询条件。</p>
 *
 * @param market 市场
 * @param code 股票代码
 * @param period 周期
 * @param limit 返回条数
 * @param apiToken API Token
 * @author Global Stock Team
 * @since 0.0.1
 */
public record MarketDataQuery(String market, String code, String period, Integer limit, String apiToken) {
}
