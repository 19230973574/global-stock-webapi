package com.globalstock.webapi.model.dto;

import java.util.List;

/**
 * API Token DTO。
 *
 * <p>职责说明：向前端返回 Token 信息与权限快照。</p>
 *
 * @param token token 值
 * @param accountEmail 账号邮箱
 * @param dataPermissions 数据权限
 * @param status 状态
 * @param createdAt 创建时间
 * @param expiresAt 过期时间
 * @author Global Stock Team
 * @since 0.0.1
 */
public record ApiTokenDTO(String id, String token, String accountEmail, List<String> dataPermissions, String status, Long createdAt, Long expiresAt) {
}
