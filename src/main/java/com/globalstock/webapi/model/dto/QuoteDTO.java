package com.globalstock.webapi.model.dto;

/**
 * 实时行情 DTO。
 *
 * <p>职责说明：基于 MongoDB 最新当日 K线生成开放实时行情响应。</p>
 *
 * @param code 股票代码
 * @param market 市场
 * @param price 最新价
 * @param open 今开
 * @param high 最高价
 * @param low 最低价
 * @param volume 成交量
 * @param turnover 成交额
 * @param change 涨跌额
 * @param changePct 涨跌幅
 * @param quoteTime 行情时间
 * @author Global Stock Team
 * @since 0.0.1
 */
public record QuoteDTO(
        String code,
        String market,
        Double price,
        Double open,
        Double high,
        Double low,
        Long volume,
        Double turnover,
        Double change,
        Double changePct,
        String quoteTime
) {
}
