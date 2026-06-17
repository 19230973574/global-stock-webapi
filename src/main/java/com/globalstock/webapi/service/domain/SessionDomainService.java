package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.common.UnauthorizedException;
import com.globalstock.webapi.infra.SessionInfra;
import com.globalstock.webapi.model.document.UserSessionDocument;
import org.springframework.stereotype.Service;

/**
 * 用户会话领域服务。
 */
@Service
public class SessionDomainService {

    private final SessionInfra sessionInfra;

    public SessionDomainService(SessionInfra sessionInfra) {
        this.sessionInfra = sessionInfra;
    }

    public UserSessionDocument createSession(String email, String role, String token, long expiresAt) {
        long now = System.currentTimeMillis();
        UserSessionDocument document = new UserSessionDocument();
        document.setTokenValue(token);
        document.setEmail(email);
        document.setRole(role);
        document.setExpiresAt(expiresAt);
        document.setCreatedAt(now);
        return sessionInfra.save(document);
    }

    public UserSessionDocument validateToken(String token) {
        UserSessionDocument session = sessionInfra.findByToken(token);
        if (session == null) {
            throw new UnauthorizedException("登录已失效，请重新登录");
        }
        if (session.getExpiresAt() != null && session.getExpiresAt() < System.currentTimeMillis()) {
            sessionInfra.deleteByToken(token);
            throw new UnauthorizedException("登录已过期，请重新登录");
        }
        return session;
    }

    public void revokeToken(String token) {
        sessionInfra.deleteByToken(token);
    }
}
