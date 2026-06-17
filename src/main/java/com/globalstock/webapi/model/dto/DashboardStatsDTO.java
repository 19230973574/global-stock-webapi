package com.globalstock.webapi.model.dto;

/**
 * 后台仪表盘统计 DTO。
 */
public record DashboardStatsDTO(
        long totalUsers,
        long activeUsers,
        long activeTokens,
        long totalPermissions,
        long pendingOrders,
        long activeSubscriptions,
        long monthlyApiCalls,
        long expiringSoon
) {
}
