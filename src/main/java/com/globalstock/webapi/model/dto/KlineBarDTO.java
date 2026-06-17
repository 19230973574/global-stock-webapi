package com.globalstock.webapi.model.dto;

/**
 * K线数据 DTO。
 *
 * <p>职责说明：对前端暴露标准化 K线字段。</p>
 *
 * @param code 股票代码
 * @param market 市场
 * @param period 周期
 * @param time 时间
 * @param open 开盘价
 * @param high 最高价
 * @param low 最低价
 * @param close 收盘价
 * @param volume 成交量
 * @param turnover 成交额
 * @param change 涨跌额
 * @param changePct 涨跌幅
 * @author Global Stock Team
 * @since 0.0.1
 */
public record KlineBarDTO(
        String code,
        String market,
        String period,
        String time,
        Double open,
        Double high,
        Double low,
        Double close,
        Long volume,
        Double turnover,
        Double change,
        Double changePct
) {
}
