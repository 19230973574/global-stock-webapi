package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.common.UnauthorizedException;
import com.globalstock.webapi.context.AuthContext;
import com.globalstock.webapi.manager.AuditManager;
import com.globalstock.webapi.model.document.PurchaseOrderDocument;
import com.globalstock.webapi.model.document.UserAccountDocument;
import com.globalstock.webapi.model.dto.ApiUsageDTO;
import com.globalstock.webapi.model.dto.AuditLogDTO;
import com.globalstock.webapi.model.dto.PurchaseOrderDTO;
import com.globalstock.webapi.model.dto.SubscriptionDTO;
import com.globalstock.webapi.model.enums.OrderSource;
import com.globalstock.webapi.model.enums.OrderStatus;
import com.globalstock.webapi.model.enums.UserRole;
import com.globalstock.webapi.model.request.CommerceRequests;
import com.globalstock.webapi.service.domain.AuthDomainService;
import com.globalstock.webapi.service.domain.CatalogDomainService;
import com.globalstock.webapi.service.domain.EntitlementDomainService;
import com.globalstock.webapi.service.domain.OrderDomainService;
import com.globalstock.webapi.service.domain.ApiUsageDomainService;
import com.globalstock.webapi.service.domain.SubscriptionDomainService;
import com.globalstock.webapi.infra.AuditLogInfra;
import com.globalstock.webapi.model.document.AuditLogDocument;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * 商务域：采购订单与审核。
 */
@Service
public class CommerceService {

    private final OrderDomainService orderDomainService;
    private final AuthDomainService authDomainService;
    private final CatalogDomainService catalogDomainService;
    private final EntitlementDomainService entitlementDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final ApiUsageDomainService apiUsageDomainService;
    private final AuditManager auditManager;
    private final AuditLogInfra auditLogInfra;

    public CommerceService(OrderDomainService orderDomainService,
                           AuthDomainService authDomainService,
                           CatalogDomainService catalogDomainService,
                           EntitlementDomainService entitlementDomainService,
                           SubscriptionDomainService subscriptionDomainService,
                           ApiUsageDomainService apiUsageDomainService,
                           AuditManager auditManager,
                           AuditLogInfra auditLogInfra) {
        this.orderDomainService = orderDomainService;
        this.authDomainService = authDomainService;
        this.catalogDomainService = catalogDomainService;
        this.entitlementDomainService = entitlementDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.apiUsageDomainService = apiUsageDomainService;
        this.auditManager = auditManager;
        this.auditLogInfra = auditLogInfra;
    }

    public PurchaseOrderDTO createOrder(CommerceRequests.CreateOrderRequest request) {
        AuthContext.SessionInfo session = requireSession();
        UserRole operatorRole = UserRole.fromCode(session.role());
        String email = session.email();
        String permission = normalizePermission(request.permission());
        UserAccountDocument account = requiredAccount(email);

        if (catalogDomainService.isBundlePermission(permission)) {
            var bundle = catalogDomainService.requireBundle(catalogDomainService.extractBundleId(permission));
            boolean allOwned = bundle.getProductIds().stream()
                    .allMatch(p -> entitlementDomainService.hasPermission(account, p));
            if (allOwned) {
                throw new BusinessException("该套餐内数据权限已全部开通");
            }
        } else if (entitlementDomainService.hasPermission(account, permission)) {
            throw new BusinessException("该数据权限已开通");
        }
        if (orderDomainService.existsPendingOrder(email, permission)) {
            throw new BusinessException("已有待审核的相同采购申请");
        }

        PurchaseOrderDocument order = orderDomainService.createOrder(email, permission, OrderSource.SELF_SERVICE);
        auditManager.log("order.submitted", email, email, "order", order.getId(),
                "提交采购申请: " + permission);

        if (operatorRole.isAtLeast(UserRole.ADMIN)) {
            return approveOrderInternal(order, email, "管理员自助采购自动通过");
        }

        return toOrderDTO(order);
    }

    public List<PurchaseOrderDTO> listMyOrders() {
        AuthContext.SessionInfo session = requireSession();
        return orderDomainService.findByAccountEmail(session.email())
                .stream()
                .map(this::toOrderDTO)
                .toList();
    }

    public PurchaseOrderDTO getMyOrder(String orderId) {
        AuthContext.SessionInfo session = requireSession();
        PurchaseOrderDocument order = requiredOrder(orderId);
        if (!order.getAccountEmail().equals(session.email())) {
            throw new UnauthorizedException("无权查看该订单", 403);
        }
        return toOrderDTO(order);
    }

    public void cancelMyOrder(String orderId) {
        AuthContext.SessionInfo session = requireSession();
        PurchaseOrderDocument order = requiredOrder(orderId);
        if (!order.getAccountEmail().equals(session.email())) {
            throw new UnauthorizedException("无权操作该订单", 403);
        }
        if (!OrderStatus.PENDING_REVIEW.getCode().equals(order.getStatus())) {
            throw new BusinessException("仅待审核订单可取消");
        }
        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderDomainService.save(order);
        auditManager.log("order.cancelled", session.email(), session.email(), "order", order.getId(),
                "取消采购申请: " + order.getPermission());
    }

    public List<PurchaseOrderDTO> listOrdersForReview(String status, int limit) {
        requireRoleAtLeast(UserRole.ADMIN);
        String normalizedStatus = status == null || status.isBlank()
                ? OrderStatus.PENDING_REVIEW.getCode()
                : status.trim().toLowerCase(Locale.ROOT);
        return orderDomainService.findByStatus(normalizedStatus, limit)
                .stream()
                .map(this::toOrderDTO)
                .toList();
    }

    public PurchaseOrderDTO approveOrder(String orderId) {
        AuthContext.SessionInfo session = requireSession();
        requireRoleAtLeast(UserRole.ADMIN);
        PurchaseOrderDocument order = requiredOrder(orderId);
        return approveOrderInternal(order, session.email(), null);
    }

    public PurchaseOrderDTO rejectOrder(String orderId, CommerceRequests.RejectOrderRequest request) {
        AuthContext.SessionInfo session = requireSession();
        requireRoleAtLeast(UserRole.ADMIN);
        PurchaseOrderDocument order = requiredOrder(orderId);
        if (!OrderStatus.PENDING_REVIEW.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不可驳回");
        }
        order.setStatus(OrderStatus.REJECTED.getCode());
        order.setReviewedAt(System.currentTimeMillis());
        order.setReviewedBy(session.email());
        order.setRejectReason(request.reason() == null ? "" : request.reason().trim());
        orderDomainService.save(order);
        auditManager.log("order.rejected", session.email(), order.getAccountEmail(), "order", order.getId(),
                "驳回采购申请: " + order.getPermission() + (order.getRejectReason().isBlank() ? "" : "，原因: " + order.getRejectReason()));
        return toOrderDTO(order);
    }

    public List<AuditLogDTO> listRecentAuditLogs(int limit) {
        requireRoleAtLeast(UserRole.ADMIN);
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return auditLogInfra.findRecent(safeLimit)
                .stream()
                .map(this::toAuditDTO)
                .toList();
    }

    public List<SubscriptionDTO> listMySubscriptions() {
        AuthContext.SessionInfo session = requireSession();
        return subscriptionDomainService.listByAccount(session.email())
                .stream()
                .map(this::toSubscriptionDTO)
                .toList();
    }

    public ApiUsageDTO getMyUsage() {
        AuthContext.SessionInfo session = requireSession();
        String email = session.email();
        Long quota = subscriptionDomainService.resolveMonthlyQuota(email);
        long used = apiUsageDomainService.getCurrentUsage(email);
        return new ApiUsageDTO(
                apiUsageDomainService.currentMonth(),
                used,
                quota,
                quota == null
        );
    }

    public long countPendingOrders() {
        return orderDomainService.countPendingOrders();
    }

    private PurchaseOrderDTO approveOrderInternal(PurchaseOrderDocument order, String reviewerEmail, String autoNote) {
        if (!OrderStatus.PENDING_REVIEW.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不可审核通过");
        }
        UserAccountDocument account = requiredAccount(order.getAccountEmail());
        entitlementDomainService.grantFromOrder(account, order.getPermission(), order.getId(), reviewerEmail);

        order.setStatus(OrderStatus.APPROVED.getCode());
        order.setReviewedAt(System.currentTimeMillis());
        order.setReviewedBy(reviewerEmail);
        orderDomainService.save(order);

        String detail = "审核通过并开通: " + order.getPermission();
        if (autoNote != null && !autoNote.isBlank()) {
            detail = detail + "（" + autoNote + "）";
        }
        auditManager.log("order.approved", reviewerEmail, order.getAccountEmail(), "order", order.getId(), detail);
        auditManager.log("permission.granted", reviewerEmail, order.getAccountEmail(), "permission",
                order.getPermission(), "来源订单: " + order.getOrderNo());

        return toOrderDTO(order);
    }

    private PurchaseOrderDocument requiredOrder(String orderId) {
        return orderDomainService.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
    }

    private UserAccountDocument requiredAccount(String email) {
        UserAccountDocument account = authDomainService.findByEmail(email);
        if (account == null) {
            throw new BusinessException("账号不存在");
        }
        return account;
    }

    private String normalizePermission(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new BusinessException("权限不能为空");
        }
        String normalized = permission.trim().toLowerCase(Locale.ROOT);
        catalogDomainService.normalizeOrderPermission(normalized);
        return normalized;
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

    private PurchaseOrderDTO toOrderDTO(PurchaseOrderDocument document) {
        return new PurchaseOrderDTO(
                document.getId(),
                document.getOrderNo(),
                document.getAccountEmail(),
                document.getPermission(),
                document.getStatus(),
                document.getSource(),
                document.getSubmittedAt(),
                document.getReviewedAt(),
                document.getReviewedBy(),
                document.getRejectReason()
        );
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

    private SubscriptionDTO toSubscriptionDTO(com.globalstock.webapi.model.document.SubscriptionDocument document) {
        String productName = catalogDomainService.findProduct(document.getProductId())
                .map(p -> p.getName())
                .orElse(document.getProductId());
        return new SubscriptionDTO(
                document.getId(),
                document.getAccountEmail(),
                document.getProductId(),
                productName,
                document.getStatus(),
                document.getStartsAt(),
                document.getExpiresAt(),
                document.getOrderId(),
                document.getGrantedBy()
        );
    }
}
