package com.globalstock.webapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用基础配置。
 *
 * <p>职责说明：提供前端基础地址，用于构造邮件链接。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Component
public class MarketAppProperties {

    @Value("${market.app.base-url}")
    private String baseUrl;

    /**
     * 获取前端基础地址。
     *
     * @return 前端基础地址
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
