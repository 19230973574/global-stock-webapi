package com.globalstock.webapi.controller;

import com.globalstock.webapi.common.ApiResponse;
import com.globalstock.webapi.model.dto.ApiTokenDTO;
import com.globalstock.webapi.model.dto.AuthSessionDTO;
import com.globalstock.webapi.model.dto.DashboardStatsDTO;
import com.globalstock.webapi.model.dto.UserAccountDTO;
import com.globalstock.webapi.model.request.AuthRequests;
import com.globalstock.webapi.service.AuthService;
import com.globalstock.webapi.service.TokenService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 邮箱认证与用户管理接口。
 */
@RestController
@RequestMapping("/auth/v1")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/registrations")
    public ApiResponse<Void> register(@RequestBody AuthRequests.RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/activations")
    public ApiResponse<Void> activate(@RequestBody AuthRequests.ActivateRequest request) {
        authService.activate(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/sessions")
    public ApiResponse<AuthSessionDTO> login(@RequestBody AuthRequests.LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<AuthSessionDTO> currentProfile() {
        return ApiResponse.success(authService.getCurrentProfile());
    }

    @GetMapping("/dashboard-stats")
    public ApiResponse<DashboardStatsDTO> dashboardStats() {
        return ApiResponse.success(authService.getDashboardStats());
    }

    @GetMapping("/users")
    public ApiResponse<List<UserAccountDTO>> listUsers() {
        return ApiResponse.success(authService.listUsers());
    }

    @PostMapping("/users")
    public ApiResponse<UserAccountDTO> createUser(@RequestBody AuthRequests.CreateUserRequest request) {
        return ApiResponse.success(authService.createUser(request));
    }

    @PatchMapping("/users/{email}/role")
    public ApiResponse<UserAccountDTO> updateUserRole(@PathVariable String email,
                                                      @RequestBody AuthRequests.UpdateRoleRequest request) {
        return ApiResponse.success(authService.updateUserRole(email, request));
    }

    @PostMapping("/password-reset-requests")
    public ApiResponse<Void> requestPasswordReset(@RequestBody AuthRequests.ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/password-resets")
    public ApiResponse<Void> resetPassword(@RequestBody AuthRequests.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/data-permissions")
    public ApiResponse<Void> grantPermission(@RequestBody AuthRequests.GrantPermissionRequest request) {
        authService.grantPermission(request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/data-permissions")
    public ApiResponse<Void> revokePermission(@RequestBody AuthRequests.RevokePermissionRequest request) {
        authService.revokePermission(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/api-tokens")
    public ApiResponse<ApiTokenDTO> generateApiToken(@RequestBody AuthRequests.GenerateApiTokenRequest request) {
        return ApiResponse.success(tokenService.generate(request));
    }

    @GetMapping("/api-tokens")
    public ApiResponse<List<ApiTokenDTO>> listApiTokens(@RequestParam String email) {
        return ApiResponse.success(tokenService.listForEmail(email));
    }
}
