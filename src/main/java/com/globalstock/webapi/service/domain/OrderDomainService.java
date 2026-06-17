package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.infra.OrderInfra;
import com.globalstock.webapi.model.document.PurchaseOrderDocument;
import com.globalstock.webapi.model.enums.OrderSource;
import com.globalstock.webapi.model.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderDomainService {

    private final OrderInfra orderInfra;

    public OrderDomainService(OrderInfra orderInfra) {
        this.orderInfra = orderInfra;
    }

    public PurchaseOrderDocument createOrder(String accountEmail, String permission, OrderSource source) {
        long now = System.currentTimeMillis();
        PurchaseOrderDocument document = new PurchaseOrderDocument();
        document.setOrderNo(generateOrderNo());
        document.setAccountEmail(accountEmail);
        document.setPermission(permission);
        document.setStatus(OrderStatus.PENDING_REVIEW.getCode());
        document.setSource(source.getCode());
        document.setSubmittedAt(now);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return orderInfra.save(document);
    }

    public Optional<PurchaseOrderDocument> findById(String id) {
        return orderInfra.findById(id);
    }

    public List<PurchaseOrderDocument> findByAccountEmail(String accountEmail) {
        return orderInfra.findByAccountEmail(accountEmail);
    }

    public List<PurchaseOrderDocument> findPendingOrders(int limit) {
        return orderInfra.findByStatus(OrderStatus.PENDING_REVIEW.getCode(), limit);
    }

    public List<PurchaseOrderDocument> findByStatus(String status, int limit) {
        return orderInfra.findByStatus(status, limit);
    }

    public long countPendingOrders() {
        return orderInfra.countByStatus(OrderStatus.PENDING_REVIEW.getCode());
    }

    public boolean existsPendingOrder(String accountEmail, String permission) {
        return orderInfra.existsPendingOrder(accountEmail, permission);
    }

    public PurchaseOrderDocument save(PurchaseOrderDocument document) {
        document.setUpdatedAt(System.currentTimeMillis());
        return orderInfra.save(document);
    }

    private String generateOrderNo() {
        return "PO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
