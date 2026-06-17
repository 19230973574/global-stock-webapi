package com.globalstock.webapi.model.request;

/**
 * 商务域请求 DTO。
 */
public final class CommerceRequests {

    private CommerceRequests() {
    }

    public record CreateOrderRequest(String permission) {
    }

    public record RejectOrderRequest(String reason) {
    }
}
