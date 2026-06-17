package com.globalstock.webapi.interceptor;

import com.globalstock.webapi.common.UnauthorizedException;
import com.globalstock.webapi.context.AuthContext;
import com.globalstock.webapi.model.document.UserSessionDocument;
import com.globalstock.webapi.service.domain.SessionDomainService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录会话拦截器，校验 Authorization Bearer Token。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionDomainService sessionDomainService;

    public AuthInterceptor(SessionDomainService sessionDomainService) {
        this.sessionDomainService = sessionDomainService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("请先登录");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        UserSessionDocument session = sessionDomainService.validateToken(token);
        AuthContext.set(new AuthContext.SessionInfo(session.getEmail(), session.getRole(), token));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AuthContext.clear();
    }
}
