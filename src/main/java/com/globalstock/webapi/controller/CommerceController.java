package com.globalstock.webapi.controller;

import com.globalstock.webapi.common.ApiResponse;
import com.globalstock.webapi.model.dto.AuditLogDTO;
import com.globalstock.webapi.model.dto.ApiUsageDTO;
import com.globalstock.webapi.model.dto.PurchaseOrderDTO;
import com.globalstock.webapi.model.dto.SubscriptionDTO;
import com.globalstock.webapi.model.request.CommerceRequests;
import com.globalstock.webapi.service.CommerceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 客户侧采购订单接口。
 */
@RestController
@RequestMapping("/commerce/v1")
public class CommerceController {

    private final CommerceService commerceService;

    public CommerceController(CommerceService commerceService) {
        this.commerceService = commerceService;
    }

    @PostMapping("/orders")
    public ApiResponse<PurchaseOrderDTO> createOrder(@RequestBody CommerceRequests.CreateOrderRequest request) {
        return ApiResponse.success(commerceService.createOrder(request));
    }

    @GetMapping("/orders")
    public ApiResponse<List<PurchaseOrderDTO>> listMyOrders() {
        return ApiResponse.success(commerceService.listMyOrders());
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<PurchaseOrderDTO> getMyOrder(@PathVariable String id) {
        return ApiResponse.success(commerceService.getMyOrder(id));
    }

    @DeleteMapping("/orders/{id}")
    public ApiResponse<Void> cancelMyOrder(@PathVariable String id) {
        commerceService.cancelMyOrder(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/subscriptions")
    public ApiResponse<List<SubscriptionDTO>> listMySubscriptions() {
        return ApiResponse.success(commerceService.listMySubscriptions());
    }

    @GetMapping("/usage")
    public ApiResponse<ApiUsageDTO> getMyUsage() {
        return ApiResponse.success(commerceService.getMyUsage());
    }
}
