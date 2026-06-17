package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.infra.ApiTokenInfra;
import com.globalstock.webapi.model.document.ApiTokenDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ApiTokenDomainService {

    private static final Logger log = LoggerFactory.getLogger(ApiTokenDomainService.class);

    private final ApiTokenInfra apiTokenInfra;

    public ApiTokenDomainService(ApiTokenInfra apiTokenInfra) {
        this.apiTokenInfra = apiTokenInfra;
    }

    public ApiTokenDocument findByTokenValue(String token) {
        ApiTokenDocument document = apiTokenInfra.findByTokenValue(token);
        if (document == null) {
            log.warn("api token not found");
        }
        return document;
    }

    public ApiTokenDocument findById(String id) {
        ApiTokenDocument document = apiTokenInfra.findById(id);
        if (document == null) {
            throw new BusinessException("API Token 不存在");
        }
        return document;
    }

    public List<ApiTokenDocument> findByAccountEmail(String email) {
        return apiTokenInfra.findByAccountEmail(email);
    }

    public ApiTokenDocument createToken(String accountEmail, String tokenValue, List<String> permissions) {
        long now = System.currentTimeMillis();
        ApiTokenDocument document = new ApiTokenDocument();
        document.setAccountEmail(accountEmail);
        document.setTokenValue(tokenValue);
        document.setDataPermissions(new ArrayList<>(permissions));
        document.setStatus("active");
        document.setCreatedAt(now);
        document.setExpiresAt(now + 365L * 24L * 60L * 60L * 1000L);
        document.setUpdatedAt(now);
        return apiTokenInfra.save(document);
    }

    public ApiTokenDocument revokeById(String id) {
        ApiTokenDocument document = findById(id);
        document.setStatus("revoked");
        document.setUpdatedAt(System.currentTimeMillis());
        return apiTokenInfra.save(document);
    }

    public ApiTokenDocument refreshPermissions(ApiTokenDocument document, Set<String> permissions) {
        document.setDataPermissions(new ArrayList<>(permissions));
        document.setUpdatedAt(System.currentTimeMillis());
        return apiTokenInfra.save(document);
    }

    public void revokeAllActiveForAccount(String accountEmail) {
        long now = System.currentTimeMillis();
        for (ApiTokenDocument token : apiTokenInfra.findByAccountEmail(accountEmail)) {
            if ("active".equals(token.getStatus())) {
                token.setStatus("revoked");
                token.setUpdatedAt(now);
                apiTokenInfra.save(token);
            }
        }
    }

    public long countActive() {
        return apiTokenInfra.countActive();
    }
}
