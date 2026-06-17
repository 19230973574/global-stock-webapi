package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.infra.AuditLogInfra;
import com.globalstock.webapi.manager.AuditManager;
import com.globalstock.webapi.model.document.AuditLogDocument;
import com.globalstock.webapi.model.document.UserAccountDocument;
import com.globalstock.webapi.model.dto.AuditLogDTO;
import com.globalstock.webapi.model.dto.DashboardStatsDTO;
import com.globalstock.webapi.model.dto.UserAccountDTO;
import com.globalstock.webapi.model.enums.UserRole;
import com.globalstock.webapi.model.request.AdminRequests;
import com.globalstock.webapi.model.request.AuthRequests;
import com.globalstock.webapi.service.domain.ApiTokenDomainService;
import com.globalstock.webapi.service.domain.ApiUsageDomainService;
import com.globalstock.webapi.service.domain.AuthDomainService;
import com.globalstock.webapi.service.domain.OrderDomainService;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AdminService {

    private final AuthService authService;
    private final AuthDomainService authDomainService;
    private final ApiTokenDomainService apiTokenDomainService;
    private final OrderDomainService orderDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final ApiUsageDomainService apiUsageDomainService;
    private final TokenService tokenService;
    private final AuditManager auditManager;
    private final AuditLogInfra auditLogInfra;

    public AdminService(AuthService authService,
                        AuthDomainService authDomainService,
                        ApiTokenDomainService apiTokenDomainService,
                        OrderDomainService orderDomainService,
                        SubscriptionDomainService subscriptionDomainService,
                        ApiUsageDomainService apiUsageDomainService,
                        TokenService tokenService,
                        AuditManager auditManager,
                        AuditLogInfra auditLogInfra) {
        this.authService = authService;
        this.authDomainService = authDomainService;
        this.apiTokenDomainService = apiTokenDomainService;
        this.orderDomainService = orderDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.apiUsageDomainService = apiUsageDomainService;
        this.tokenService = tokenService;
        this.auditManager = auditManager;
        this.auditLogInfra = auditLogInfra;
    }

    public DashboardStatsDTO getDashboardStats() {
        authService.requireAdminRole();
        long totalUsers = authDomainService.countAll();
        long activeUsers = authDomainService.countActive();
        long activeTokens = apiTokenDomainService.countActive();
        long totalPermissions = authDomainService.findAll().stream()
                .mapToLong(account -> account.getDataPermissions() == null ? 0 : account.getDataPermissions().size())
                .sum();
        return new DashboardStatsDTO(
                totalUsers,
                activeUsers,
                activeTokens,
                totalPermissions,
                orderDomainService.countPendingOrders(),
                subscriptionDomainService.countActiveSubscriptions(),
                apiUsageDomainService.sumCallsThisMonth(),
                subscriptionDomainService.countExpiringWithinDays(7)
        );
    }

    public List<UserAccountDTO> listUsers() {
        return authService.listUsers();
    }

    public UserAccountDTO createUser(AuthRequests.CreateUserRequest request) {
        return authService.createUser(request);
    }

    public UserAccountDTO updateUserRole(String email, AuthRequests.UpdateRoleRequest request) {
        return authService.updateUserRole(email, request);
    }

    public UserAccountDTO updateUserStatus(String email, AdminRequests.UpdateStatusRequest request) {
        authService.requireSuperAdminRole();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String status = request.status() == null ? "" : request.status().trim().toLowerCase(Locale.ROOT);
        if (!"active".equals(status) && !"disabled".equals(status)) {
            throw new BusinessException("状态仅支持 active 或 disabled");
        }
        UserAccountDocument account = authDomainService.findByEmail(normalizedEmail);
        if (account == null) {
            throw new BusinessException("账号不存在");
        }
        String before = account.getStatus();
        account.setStatus(status);
        authDomainService.save(account);
        if ("disabled".equals(status)) {
            tokenService.revokeAllForAccount(normalizedEmail);
        }
        auditManager.log("user.status_updated", authService.currentOperatorEmail(), normalizedEmail,
                "user", normalizedEmail, "状态变更: " + before + " -> " + status);
        return authService.toUserAccountDto(account);
    }

    public List<AuditLogDTO> listAuditLogs(String type, int limit) {
        authService.requireAdminRole();
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        List<AuditLogDocument> logs = type == null || type.isBlank()
                ? auditLogInfra.findRecent(safeLimit)
                : auditLogInfra.findRecentByType(type.trim(), safeLimit);
        return logs.stream().map(this::toAuditDTO).toList();
    }

    private AuditLogDTO toAuditDTO(AuditLogDocument document) {
        return new AuditLogDTO(
                document.getId(),
                document.getAction(),
                document.getOperatorEmail(),
                document.getTargetEmail(),
                document.getResourceType(),
                document.getResourceId(),
                document.getDetail(),
                document.getBefore(),
                document.getAfter(),
                document.getCreatedAt()
        );
    }
}
