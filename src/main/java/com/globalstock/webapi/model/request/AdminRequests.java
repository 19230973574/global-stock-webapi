package com.globalstock.webapi.model.request;

public final class AdminRequests {

    private AdminRequests() {
    }

    public record UpdateStatusRequest(String status) {
    }
}
