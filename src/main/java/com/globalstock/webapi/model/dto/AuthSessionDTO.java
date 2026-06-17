package com.globalstock.webapi.model.dto;

import java.util.List;

/**
 * 登录会话 DTO。
 */
public record AuthSessionDTO(
        String token,
        String email,
        String name,
        String status,
        String role,
        List<String> dataPermissions
) {
}
