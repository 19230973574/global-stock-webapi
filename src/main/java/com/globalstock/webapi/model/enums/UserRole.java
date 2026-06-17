package com.globalstock.webapi.model.enums;

import java.util.Locale;

/**
 * 后台用户角色。
 *
 * <p>SUPER_ADMIN：超级系统管理员，可管理全部用户与角色。</p>
 * <p>ADMIN：管理员，可管理普通成员与数据权限。</p>
 * <p>MEMBER：普通成员，仅可自助采购与管理自己的 Token。</p>
 */
public enum UserRole {

    SUPER_ADMIN("super_admin", "超级系统管理员"),
    ADMIN("admin", "管理员"),
    MEMBER("member", "普通成员");

    private final String code;
    private final String label;

    UserRole(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static UserRole fromCode(String code) {
        if (code == null || code.isBlank()) {
            return MEMBER;
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        for (UserRole role : values()) {
            if (role.code.equals(normalized)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知角色: " + code);
    }

    public boolean isAtLeast(UserRole required) {
        return this.ordinal() <= required.ordinal();
    }
}
