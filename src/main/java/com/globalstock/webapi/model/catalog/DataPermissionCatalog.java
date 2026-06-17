package com.globalstock.webapi.model.catalog;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 数据权限目录（Phase 1 硬编码，Phase 2 迁移至 catalog 集合）。
 */
public final class DataPermissionCatalog {

    public static final String US_MARKET_OPEN = "us_market_open";

    private static final List<String> ALL = List.of(
            US_MARKET_OPEN,
            "us_realtime",
            "us_timeshare",
            "us_history",
            "a_realtime",
            "etf_history"
    );

    private static final Set<String> US_CAPABILITIES = Set.of("us_realtime", "us_timeshare", "us_history");

    private DataPermissionCatalog() {
    }

    public static List<String> all() {
        return ALL;
    }

    public static boolean isSupported(String permission) {
        return permission != null && ALL.contains(permission.trim().toLowerCase(Locale.ROOT));
    }

    public static String normalize(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("权限不能为空");
        }
        String normalized = permission.trim().toLowerCase(Locale.ROOT);
        if (!ALL.contains(normalized)) {
            throw new IllegalArgumentException("不支持的数据权限");
        }
        return normalized;
    }

    /**
     * 美股能力权限通常依赖开盘准入。
     */
    public static boolean requiresUsMarketOpen(String permission) {
        return US_CAPABILITIES.contains(permission);
    }
}
