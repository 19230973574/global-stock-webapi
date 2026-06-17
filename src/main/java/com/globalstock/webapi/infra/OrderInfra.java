package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.OrderMapper;
import com.globalstock.webapi.model.document.PurchaseOrderDocument;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderInfra {

    private final OrderMapper orderMapper;

    public OrderInfra(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public PurchaseOrderDocument save(PurchaseOrderDocument document) {
        try {
            return orderMapper.save(document);
        } catch (Exception exception) {
            throw new SystemException("保存订单失败", exception);
        }
    }

    public Optional<PurchaseOrderDocument> findById(String id) {
        try {
            return orderMapper.findById(id);
        } catch (Exception exception) {
            throw new SystemException("查询订单失败", exception);
        }
    }

    public List<PurchaseOrderDocument> findByAccountEmail(String accountEmail) {
        try {
            return orderMapper.findByAccountEmail(accountEmail);
        } catch (Exception exception) {
            throw new SystemException("查询订单列表失败", exception);
        }
    }

    public List<PurchaseOrderDocument> findByStatus(String status, int limit) {
        try {
            return orderMapper.findByStatus(status, limit);
        } catch (Exception exception) {
            throw new SystemException("查询订单列表失败", exception);
        }
    }

    public long countByStatus(String status) {
        try {
            return orderMapper.countByStatus(status);
        } catch (Exception exception) {
            throw new SystemException("统计订单失败", exception);
        }
    }

    public boolean existsPendingOrder(String accountEmail, String permission) {
        try {
            return orderMapper.existsPendingOrder(accountEmail, permission);
        } catch (Exception exception) {
            throw new SystemException("查询订单失败", exception);
        }
    }
}
