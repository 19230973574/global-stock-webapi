package com.globalstock.webapi.model.request;

/**
 * 认证请求 DTO 集合。
 *
 * <p>职责说明：承载注册、登录、激活和重置密码请求参数。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
public final class AuthRequests {

    private AuthRequests() {
    }

    public record RegisterRequest(String name, String email, String password) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record ActivateRequest(String email, String code) {
    }

    public record ForgotPasswordRequest(String email) {
    }

    public record ResetPasswordRequest(String email, String token, String password) {
    }

    public record GrantPermissionRequest(String email, String permission) {
    }

    public record GenerateApiTokenRequest(String email) {
    }

    public record RevokePermissionRequest(String email, String permission) {
    }

    public record CreateUserRequest(String name, String email, String password, String role) {
    }

    public record UpdateRoleRequest(String role) {
    }
}
