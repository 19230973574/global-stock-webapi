package com.globalstock.webapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 跨域配置。
 *
 * <p>职责说明：提供允许访问后端接口的前端来源地址。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Component
public class CorsProperties {

    @Value("${market.cors.allowed-origins[0]:http://localhost:3001}")
    private String allowedOrigin1;

    @Value("${market.cors.allowed-origins[1]:http://127.0.0.1:3001}")
    private String allowedOrigin2;

    @Value("${market.cors.allowed-origins[2]:}")
    private String allowedOrigin3;

    /**
     * 获取允许访问的来源列表。
     *
     * @return 来源列表
     */
    public List<String> getAllowedOrigins() {
        Set<String> origins = new LinkedHashSet<>();
        origins.add("http://localhost:3001");
        origins.add("http://127.0.0.1:3001");
        origins.add("http://203.91.72.69:3001");
        addOrigin(origins, allowedOrigin1);
        addOrigin(origins, allowedOrigin2);
        addOrigin(origins, allowedOrigin3);
        return new ArrayList<>(origins);
    }

    private void addOrigin(Set<String> origins, String origin) {
        if (origin != null && !origin.isBlank()) {
            origins.add(origin.trim());
        }
    }
}
