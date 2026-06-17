package com.globalstock.webapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 认证相关配置。
 */
@Component
public class AuthProperties {

    @Value("${market.auth.session-ttl-hours:72}")
    private long sessionTtlHours;

    @Value("${market.auth.bootstrap-super-admin-emails:}")
    private String bootstrapSuperAdminEmails;

    public long getSessionTtlMillis() {
        return sessionTtlHours * 60L * 60L * 1000L;
    }

    public List<String> getBootstrapSuperAdminEmails() {
        if (bootstrapSuperAdminEmails == null || bootstrapSuperAdminEmails.isBlank()) {
            return List.of();
        }
        return Arrays.stream(bootstrapSuperAdminEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .toList();
    }
}
