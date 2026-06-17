package com.globalstock.webapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web 配置。
 *
 * <p>职责说明：配置前端开发环境跨域访问。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
            "http://localhost:3001",
            "http://127.0.0.1:3001"
    );

    private final Environment environment;

    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * 配置 CORS。
     *
     * @param registry CORS 注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(resolveAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    private List<String> resolveAllowedOrigins() {
        List<String> origins = environment.getProperty("market.cors.allowed-origins", List.class);
        return origins == null || origins.isEmpty() ? DEFAULT_ALLOWED_ORIGINS : origins;
    }
}
