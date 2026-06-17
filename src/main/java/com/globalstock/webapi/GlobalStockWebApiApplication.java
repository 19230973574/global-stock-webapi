package com.globalstock.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Global Stock 行情服务启动入口。
 *
 * <p>职责说明：加载 Web API、MongoDB 数据访问与行情领域能力。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class GlobalStockWebApiApplication {

    /**
     * 启动 Spring Boot 应用。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(GlobalStockWebApiApplication.class, args);
    }
}
