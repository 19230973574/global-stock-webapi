package com.globalstock.webapi.model.dto;

import java.util.List;

/**
 * 用户账号 DTO。
 */
public record UserAccountDTO(
        String email,
        String name,
        String status,
        String role,
        List<String> dataPermissions,
        Long lastLoginAt,
        Long createdAt
) {
}
