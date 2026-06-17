package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.common.UnauthorizedException;
import com.globalstock.webapi.config.AuthProperties;
import com.globalstock.webapi.context.AuthContext;
import com.globalstock.webapi.manager.AuditManager;
import com.globalstock.webapi.manager.EmailManager;
import com.globalstock.webapi.model.document.ApiTokenDocument;
import com.globalstock.webapi.model.document.UserAccountDocument;
import com.globalstock.webapi.model.dto.ApiTokenDTO;
import com.globalstock.webapi.model.dto.AuthSessionDTO;
import com.globalstock.webapi.model.dto.DashboardStatsDTO;
import com.globalstock.webapi.model.dto.UserAccountDTO;
import com.globalstock.webapi.model.enums.UserRole;
import com.globalstock.webapi.model.request.AuthRequests;
import com.globalstock.webapi.service.domain.ApiTokenDomainService;
import com.globalstock.webapi.service.domain.AuthDomainService;
import com.globalstock.webapi.service.domain.CatalogDomainService;
import com.globalstock.webapi.service.domain.EntitlementDomainService;
import com.globalstock.webapi.service.domain.OrderDomainService;
import com.globalstock.webapi.service.domain.SessionDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 认证业务编排服务。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final long ACTIVATION_TTL_MILLIS = 24L * 60L * 60L * 1000L;
    private static final long RESET_TTL_MILLIS = 15L * 60L * 1000L;
    private final AuthDomainService authDomainService;
    private final ApiTokenDomainService apiTokenDomainService;
    private final SessionDomainService sessionDomainService;
    private final EntitlementDomainService entitlementDomainService;
    private final CatalogDomainService catalogDomainService;
    private final OrderDomainService orderDomainService;
    private final AuditManager auditManager;
    private final EmailManager emailManager;
    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthDomainService authDomainService,
                       ApiTokenDomainService apiTokenDomainService,
                       SessionDomainService sessionDomainService,
                       EntitlementDomainService entitlementDomainService,
                       CatalogDomainService catalogDomainService,
                       OrderDomainService orderDomainService,
                       AuditManager auditManager,
                       EmailManager emailManager,
                       AuthProperties authProperties) {
        this.authDomainService = authDomainService;
        this.apiTokenDomainService = apiTokenDomainService;
        this.sessionDomainService = sessionDomainService;
        this.entitlementDomainService = entitlementDomainService;
        this.catalogDomainService = catalogDomainService;
        this.orderDomainService = orderDomainService;
        this.auditManager = auditManager;
        this.emailManager = emailManager;
        this.authProperties = authProperties;
    }

    public void register(AuthRequests.RegisterRequest request) {
        long start = System.currentTimeMillis();
        log.info("register start email={}", request.email());
        try {
            String email = normalizeEmail(request.email());
            String name = normalizeName(request.name());
            validatePassword(request.password());
            UserAccountDocument existingAccount = authDomainService.findByEmail(email);
            String code = generateToken();
            if (existingAccount != null) {
                resendActivationForPendingAccount(existingAccount, name, request.password(), code);
                emailManager.sendActivationEmail(email, code);
                log.info("register resend activation success email={}, cost={}ms", email, System.currentTimeMillis() - start);
                return;
            }

            authDomainService.createAccount(
                    name,
                    email,
                    passwordEncoder.encode(request.password()),
                    code,
                    System.currentTimeMillis() + ACTIVATION_TTL_MILLIS,
                    UserRole.MEMBER.getCode()
            );
            emailManager.sendActivationEmail(email, code);
            log.info("register success email={}, cost={}ms", email, System.currentTimeMillis() - start);
        } catch (BusinessException exception) {
            log.warn("register business failed email={}, message={}", request.email(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("register system failed email={}", request.email(), exception);
            throw exception;
        }
    }

    public void activate(AuthRequests.ActivateRequest request) {
        long start = System.currentTimeMillis();
        log.info("activate start email={}", request.email());
        try {
            String email = normalizeEmail(request.email());
            UserAccountDocument account = requiredAccount(email);
            if ("active".equals(account.getStatus())) {
                return;
            }
            if (account.getActivationCode() == null || !account.getActivationCode().equals(request.code())) {
                throw new BusinessException("激活码无效");
            }
            if (account.getActivationExpiresAt() == null || account.getActivationExpiresAt() < System.currentTimeMillis()) {
                throw new BusinessException("激活链接已过期");
            }
            account.setStatus("active");
            account.setActivationCode(null);
            account.setActivationExpiresAt(null);
            authDomainService.save(account);
            log.info("activate success email={}, cost={}ms", email, System.currentTimeMillis() - start);
        } catch (BusinessException exception) {
            log.warn("activate business failed email={}, message={}", request.email(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("activate system failed email={}", request.email(), exception);
            throw exception;
        }
    }

    public AuthSessionDTO login(AuthRequests.LoginRequest request) {
        long start = System.currentTimeMillis();
        log.info("login start email={}", request.email());
        try {
            String email = normalizeEmail(request.email());
            UserAccountDocument account = requiredAccount(email);
            if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
                throw new BusinessException("邮箱或密码错误");
            }
            if ("disabled".equals(account.getStatus())) {
                throw new BusinessException("账号已禁用");
            }
            if (!"active".equals(account.getStatus())) {
                throw new BusinessException("账号未激活");
            }

            String token = UUID.randomUUID().toString();
            UserRole role = resolveRole(account);
            long expiresAt = System.currentTimeMillis() + authProperties.getSessionTtlMillis();
            sessionDomainService.createSession(email, role.getCode(), token, expiresAt);

            account.setLastLoginAt(System.currentTimeMillis());
            authDomainService.save(account);

            log.info("login success email={}, role={}, cost={}ms", email, role.getCode(), System.currentTimeMillis() - start);
            return toSessionDTO(token, account);
        } catch (BusinessException exception) {
            log.warn("login business failed email={}, message={}", request.email(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("login system failed email={}", request.email(), exception);
            throw exception;
        }
    }

    public void logout() {
        AuthContext.SessionInfo session = requireSession();
        sessionDomainService.revokeToken(session.token());
    }

    public AuthSessionDTO getCurrentProfile() {
        AuthContext.SessionInfo session = requireSession();
        UserAccountDocument account = requiredAccount(session.email());
        return toSessionDTO(session.token(), account);
    }

    public DashboardStatsDTO getDashboardStats() {
        requireRoleAtLeast(UserRole.ADMIN);
        long totalUsers = authDomainService.countAll();
        long activeUsers = authDomainService.countActive();
        long activeTokens = apiTokenDomainService.countActive();
        long totalPermissions = authDomainService.findAll().stream()
                .mapToLong(account -> account.getDataPermissions() == null ? 0 : account.getDataPermissions().size())
                .sum();
        return new DashboardStatsDTO(totalUsers, activeUsers, activeTokens, totalPermissions,
                orderDomainService.countPendingOrders(), 0, 0, 0);
    }

    public void requireAdminRole() {
        requireRoleAtLeast(UserRole.ADMIN);
    }

    public void requireSuperAdminRole() {
        requireRoleAtLeast(UserRole.SUPER_ADMIN);
    }

    public String currentOperatorEmail() {
        return requireSession().email();
    }

    public UserAccountDTO toUserAccountDto(UserAccountDocument account) {
        return toUserAccountDTO(account);
    }

    public List<UserAccountDTO> listUsers() {
        UserRole operatorRole = requireRoleAtLeast(UserRole.ADMIN);
        return authDomainService.findAll().stream()
                .filter(account -> canViewUser(operatorRole, resolveRole(account)))
                .sorted(Comparator.comparing(UserAccountDocument::getCreatedAt, Comparator.nullsLast(Long::compareTo)).reversed())
                .map(this::toUserAccountDTO)
                .toList();
    }

    public UserAccountDTO createUser(AuthRequests.CreateUserRequest request) {
        UserRole operatorRole = requireRoleAtLeast(UserRole.ADMIN);
        String email = normalizeEmail(request.email());
        String name = normalizeName(request.name());
        validatePassword(request.password());
        UserRole targetRole = parseAssignableRole(operatorRole, request.role());

        if (authDomainService.findByEmail(email) != null) {
            throw new BusinessException("邮箱已注册");
        }

        UserAccountDocument account = new UserAccountDocument();
        account.setName(name);
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setStatus("active");
        account.setRole(targetRole.getCode());
        long now = System.currentTimeMillis();
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        authDomainService.save(account);
        return toUserAccountDTO(account);
    }

    public UserAccountDTO updateUserRole(String email, AuthRequests.UpdateRoleRequest request) {
        requireRoleAtLeast(UserRole.SUPER_ADMIN);
        String normalizedEmail = normalizeEmail(email);
        UserAccountDocument account = requiredAccount(normalizedEmail);
        UserRole newRole = UserRole.fromCode(request.role());
        account.setRole(newRole.getCode());
        authDomainService.save(account);
        return toUserAccountDTO(account);
    }

    public void requestPasswordReset(AuthRequests.ForgotPasswordRequest request) {
        long start = System.currentTimeMillis();
        log.info("request password reset start email={}", request.email());
        try {
            String email = normalizeEmail(request.email());
            UserAccountDocument account = authDomainService.findByEmail(email);
            if (account != null) {
                String token = generateToken();
                account.setResetToken(token);
                account.setResetExpiresAt(System.currentTimeMillis() + RESET_TTL_MILLIS);
                authDomainService.save(account);
                emailManager.sendResetPasswordEmail(email, token);
            }
            log.info("request password reset success email={}, cost={}ms", email, System.currentTimeMillis() - start);
        } catch (BusinessException exception) {
            log.warn("request password reset business failed email={}, message={}", request.email(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("request password reset system failed email={}", request.email(), exception);
            throw exception;
        }
    }

    public void resetPassword(AuthRequests.ResetPasswordRequest request) {
        long start = System.currentTimeMillis();
        log.info("reset password start email={}", request.email());
        try {
            String email = normalizeEmail(request.email());
            validatePassword(request.password());
            UserAccountDocument account = requiredAccount(email);
            if (account.getResetToken() == null || !account.getResetToken().equals(request.token())) {
                throw new BusinessException("重置链接无效");
            }
            if (account.getResetExpiresAt() == null || account.getResetExpiresAt() < System.currentTimeMillis()) {
                throw new BusinessException("重置链接已过期");
            }
            account.setPasswordHash(passwordEncoder.encode(request.password()));
            account.setResetToken(null);
            account.setResetExpiresAt(null);
            authDomainService.save(account);
            log.info("reset password success email={}, cost={}ms", email, System.currentTimeMillis() - start);
        } catch (BusinessException exception) {
            log.warn("reset password business failed email={}, message={}", request.email(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("reset password system failed email={}", request.email(), exception);
            throw exception;
        }
    }

    public void grantPermission(AuthRequests.GrantPermissionRequest request) {
        long start = System.currentTimeMillis();
        log.info("grant permission start email={}, permission={}", request.email(), request.permission());
        try {
            AuthContext.SessionInfo session = requireSession();
            UserRole operatorRole = UserRole.fromCode(session.role());
            String email = normalizeEmail(request.email());
            String permission = normalizePermission(request.permission());
            UserAccountDocument account = requiredAccount(email);
            assertCanManageAccount(operatorRole, session.email(), account);
            assertCanDirectGrant(operatorRole, session.email(), account);

            entitlementDomainService.grantPermission(account, permission, session.email());
            auditManager.log("permission.granted", session.email(), email, "permission", permission,
                    "管理员直接开通");

            log.info("grant permission success email={}, permission={}, cost={}ms",
                    email, permission, System.currentTimeMillis() - start);
        } catch (BusinessException | UnauthorizedException exception) {
            log.warn("grant permission failed email={}, message={}", request.email(), exception.getMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("grant permission system failed email={}", request.email(), exception);
            throw exception;
        }
    }

    public void revokePermission(AuthRequests.RevokePermissionRequest request) {
        AuthContext.SessionInfo session = requireSession();
        UserRole operatorRole = UserRole.fromCode(session.role());
        String email = normalizeEmail(request.email());
        String permission = normalizePermission(request.permission());
        UserAccountDocument account = requiredAccount(email);
        assertCanManageAccount(operatorRole, session.email(), account);
        assertCanDirectGrant(operatorRole, session.email(), account);

        entitlementDomainService.revokePermission(account, permission);
        auditManager.log("permission.revoked", session.email(), email, "permission", permission,
                "管理员直接撤销");
    }

    private AuthContext.SessionInfo requireSession() {
        AuthContext.SessionInfo session = AuthContext.get();
        if (session == null) {
            throw new UnauthorizedException("请先登录");
        }
        return session;
    }

    private UserRole requireRoleAtLeast(UserRole required) {
        AuthContext.SessionInfo session = requireSession();
        UserRole role = UserRole.fromCode(session.role());
        if (!role.isAtLeast(required)) {
            throw new UnauthorizedException("权限不足", 403);
        }
        return role;
    }

    private void assertCanDirectGrant(UserRole operatorRole, String operatorEmail, UserAccountDocument target) {
        if (operatorRole.isAtLeast(UserRole.ADMIN)) {
            return;
        }
        if (operatorEmail.equals(target.getEmail())) {
            throw new BusinessException("请通过数据采购提交申请，等待管理员审核");
        }
        throw new UnauthorizedException("无权操作该账号", 403);
    }

    private void assertCanManageAccount(UserRole operatorRole, String operatorEmail, UserAccountDocument target) {
        if (operatorEmail.equals(target.getEmail())) {
            return;
        }
        if (operatorRole == UserRole.SUPER_ADMIN) {
            return;
        }
        if (operatorRole == UserRole.ADMIN && resolveRole(target) == UserRole.MEMBER) {
            return;
        }
        throw new UnauthorizedException("无权操作该账号", 403);
    }

    private boolean canViewUser(UserRole operatorRole, UserRole targetRole) {
        if (operatorRole == UserRole.SUPER_ADMIN) {
            return true;
        }
        return operatorRole == UserRole.ADMIN && targetRole == UserRole.MEMBER;
    }

    private UserRole parseAssignableRole(UserRole operatorRole, String roleCode) {
        UserRole targetRole = roleCode == null || roleCode.isBlank()
                ? UserRole.MEMBER
                : UserRole.fromCode(roleCode);
        if (operatorRole == UserRole.ADMIN && targetRole != UserRole.MEMBER) {
            throw new UnauthorizedException("管理员仅可创建普通成员", 403);
        }
        if (operatorRole == UserRole.SUPER_ADMIN) {
            return targetRole;
        }
        return UserRole.MEMBER;
    }

    private UserRole resolveRole(UserAccountDocument account) {
        if (account.getRole() == null || account.getRole().isBlank()) {
            return UserRole.MEMBER;
        }
        return UserRole.fromCode(account.getRole());
    }

    private String normalizeEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException("邮箱格式不正确");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return "Global Stock User";
        }
        return name.trim();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("密码至少需要 8 位");
        }
    }

    private UserAccountDocument requiredAccount(String email) {
        UserAccountDocument account = authDomainService.findByEmail(email);
        if (account == null) {
            throw new BusinessException("账号不存在");
        }
        return account;
    }

    private void resendActivationForPendingAccount(UserAccountDocument account, String name, String password, String code) {
        if ("active".equals(account.getStatus())) {
            throw new BusinessException("邮箱已注册");
        }
        account.setName(name);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setStatus("pending_activation");
        account.setRole(UserRole.MEMBER.getCode());
        account.setActivationCode(code);
        account.setActivationExpiresAt(System.currentTimeMillis() + ACTIVATION_TTL_MILLIS);
        authDomainService.save(account);
    }

    private String normalizePermission(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new BusinessException("权限不能为空");
        }
        String normalized = permission.trim().toLowerCase(Locale.ROOT);
        catalogDomainService.normalizeProductId(normalized);
        return normalized;
    }

    private AuthSessionDTO toSessionDTO(String token, UserAccountDocument account) {
        List<String> permissions = account.getDataPermissions() == null ? List.of() : List.copyOf(account.getDataPermissions());
        return new AuthSessionDTO(
                token,
                account.getEmail(),
                account.getName(),
                account.getStatus(),
                resolveRole(account).getCode(),
                permissions
        );
    }

    private UserAccountDTO toUserAccountDTO(UserAccountDocument account) {
        List<String> permissions = account.getDataPermissions() == null ? List.of() : List.copyOf(account.getDataPermissions());
        return new UserAccountDTO(
                account.getEmail(),
                account.getName(),
                account.getStatus(),
                resolveRole(account).getCode(),
                permissions,
                account.getLastLoginAt(),
                account.getCreatedAt()
        );
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
