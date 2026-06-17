package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.common.UnauthorizedException;
import com.globalstock.webapi.context.AuthContext;
import com.globalstock.webapi.manager.AuditManager;
import com.globalstock.webapi.model.document.ApiTokenDocument;
import com.globalstock.webapi.model.document.UserAccountDocument;
import com.globalstock.webapi.model.dto.ApiTokenDTO;
import com.globalstock.webapi.model.dto.TokenUsageDTO;
import com.globalstock.webapi.model.enums.UserRole;
import com.globalstock.webapi.model.request.AuthRequests;
import com.globalstock.webapi.service.domain.ApiTokenDomainService;
import com.globalstock.webapi.service.domain.AuthDomainService;
import com.globalstock.webapi.service.domain.CatalogDomainService;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Service
public class TokenService {

    private final ApiTokenDomainService apiTokenDomainService;
    private final AuthDomainService authDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final CatalogDomainService catalogDomainService;
    private final UsageService usageService;
    private final AuditManager auditManager;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(ApiTokenDomainService apiTokenDomainService,
                        AuthDomainService authDomainService,
                        SubscriptionDomainService subscriptionDomainService,
                        CatalogDomainService catalogDomainService,
                        UsageService usageService,
                        AuditManager auditManager) {
        this.apiTokenDomainService = apiTokenDomainService;
        this.authDomainService = authDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.catalogDomainService = catalogDomainService;
        this.usageService = usageService;
        this.auditManager = auditManager;
    }

    public ApiTokenDTO generateForCurrentUser() {
        AuthContext.SessionInfo session = requireSession();
        return generateForEmail(session.email());
    }

    public ApiTokenDTO generateForEmail(String email) {
        AuthContext.SessionInfo session = requireSession();
        UserAccountDocument account = requiredAccount(email);
        assertCanManageAccount(UserRole.fromCode(session.role()), session.email(), account);

        Set<String> permissions = subscriptionDomainService.resolveActiveProductIds(email);
        if (permissions.isEmpty()) {
            throw new BusinessException("请先采购数据权限后再生成 Token");
        }
        catalogDomainService.resolveMarketOpenPermission("US").ifPresent(openPerm -> {
            if (!permissions.contains(openPerm)) {
                throw new BusinessException("请先采购美股开盘权限后再生成 Token");
            }
        });

        ApiTokenDocument token = apiTokenDomainService.createToken(email, generateTokenValue(), new ArrayList<>(permissions));
        auditManager.log("token.generated", session.email(), email, "token", token.getId(), "生成 API Token");
        return toDto(token);
    }

    /** @deprecated 兼容旧 auth 接口 */
    public ApiTokenDTO generate(AuthRequests.GenerateApiTokenRequest request) {
        return generateForEmail(request.email());
    }

    public List<ApiTokenDTO> listForCurrentUser() {
        AuthContext.SessionInfo session = requireSession();
        return listForEmail(session.email());
    }

    public List<ApiTokenDTO> listForEmail(String email) {
        AuthContext.SessionInfo session = requireSession();
        UserAccountDocument account = requiredAccount(email);
        assertCanManageAccount(UserRole.fromCode(session.role()), session.email(), account);
        return apiTokenDomainService.findByAccountEmail(email).stream().map(this::toDto).toList();
    }

    public void revoke(String tokenId) {
        AuthContext.SessionInfo session = requireSession();
        ApiTokenDocument token = apiTokenDomainService.findById(tokenId);
        UserAccountDocument account = requiredAccount(token.getAccountEmail());
        assertCanManageAccount(UserRole.fromCode(session.role()), session.email(), account);
        apiTokenDomainService.revokeById(tokenId);
        auditManager.log("token.revoked", session.email(), token.getAccountEmail(), "token", tokenId, "吊销 API Token");
    }

    public ApiTokenDTO rotate(String tokenId) {
        AuthContext.SessionInfo session = requireSession();
        ApiTokenDocument oldToken = apiTokenDomainService.findById(tokenId);
        UserAccountDocument account = requiredAccount(oldToken.getAccountEmail());
        assertCanManageAccount(UserRole.fromCode(session.role()), session.email(), account);

        apiTokenDomainService.revokeById(tokenId);
        Set<String> permissions = subscriptionDomainService.resolveActiveProductIds(oldToken.getAccountEmail());
        ApiTokenDocument newToken = apiTokenDomainService.createToken(
                oldToken.getAccountEmail(), generateTokenValue(), new ArrayList<>(permissions));
        auditManager.log("token.rotated", session.email(), oldToken.getAccountEmail(), "token", newToken.getId(),
                "轮换 Token，旧 ID=" + tokenId);
        return toDto(newToken);
    }

    public ApiTokenDTO refreshPermissions(String tokenId) {
        AuthContext.SessionInfo session = requireSession();
        ApiTokenDocument token = apiTokenDomainService.findById(tokenId);
        UserAccountDocument account = requiredAccount(token.getAccountEmail());
        assertCanManageAccount(UserRole.fromCode(session.role()), session.email(), account);

        Set<String> permissions = subscriptionDomainService.resolveActiveProductIds(token.getAccountEmail());
        ApiTokenDocument updated = apiTokenDomainService.refreshPermissions(token, permissions);
        auditManager.log("token.refreshed", session.email(), token.getAccountEmail(), "token", tokenId,
                "刷新 Token 权限快照");
        return toDto(updated);
    }

    public void refreshAllForAccount(String accountEmail) {
        Set<String> permissions = subscriptionDomainService.resolveActiveProductIds(accountEmail);
        for (ApiTokenDocument token : apiTokenDomainService.findByAccountEmail(accountEmail)) {
            if ("active".equals(token.getStatus())) {
                apiTokenDomainService.refreshPermissions(token, permissions);
            }
        }
    }

    public void revokeAllForAccount(String accountEmail) {
        apiTokenDomainService.revokeAllActiveForAccount(accountEmail);
    }

    public TokenUsageDTO getTokenUsage(String tokenId) {
        AuthContext.SessionInfo session = requireSession();
        ApiTokenDocument token = apiTokenDomainService.findById(tokenId);
        assertCanManageAccount(UserRole.fromCode(session.role()), session.email(),
                requiredAccount(token.getAccountEmail()));
        long count = usageService.countTokenUsageThisMonth(token.getTokenValue());
        return new TokenUsageDTO(tokenId, usageService.getCurrentMonth(), count);
    }

    String getCurrentMonth() {
        return usageService.getCurrentMonth();
    }

    private UserAccountDocument requiredAccount(String email) {
        UserAccountDocument account = authDomainService.findByEmail(email);
        if (account == null) {
            throw new BusinessException("账号不存在");
        }
        return account;
    }

    private void assertCanManageAccount(UserRole operatorRole, String operatorEmail, UserAccountDocument target) {
        if (operatorEmail.equals(target.getEmail())) {
            return;
        }
        if (operatorRole == UserRole.SUPER_ADMIN) {
            return;
        }
        if (operatorRole == UserRole.ADMIN && UserRole.MEMBER.getCode().equals(target.getRole())) {
            return;
        }
        throw new UnauthorizedException("无权操作该账号", 403);
    }

    private AuthContext.SessionInfo requireSession() {
        AuthContext.SessionInfo session = AuthContext.get();
        if (session == null) {
            throw new UnauthorizedException("请先登录");
        }
        return session;
    }

    private ApiTokenDTO toDto(ApiTokenDocument document) {
        List<String> permissions = document.getDataPermissions() == null
                ? List.of()
                : List.copyOf(document.getDataPermissions());
        return new ApiTokenDTO(
                document.getId(),
                document.getTokenValue(),
                document.getAccountEmail(),
                permissions,
                document.getStatus(),
                document.getCreatedAt(),
                document.getExpiresAt()
        );
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
