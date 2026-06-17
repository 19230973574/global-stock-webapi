package com.globalstock.webapi.config;

import com.globalstock.webapi.model.document.UserAccountDocument;
import com.globalstock.webapi.model.enums.UserRole;
import com.globalstock.webapi.service.domain.AuthDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时提升配置的邮箱为超级系统管理员。
 */
@Component
public class AuthBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthBootstrap.class);

    private final AuthDomainService authDomainService;
    private final AuthProperties authProperties;

    public AuthBootstrap(AuthDomainService authDomainService, AuthProperties authProperties) {
        this.authDomainService = authDomainService;
        this.authProperties = authProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String email : authProperties.getBootstrapSuperAdminEmails()) {
            UserAccountDocument account = authDomainService.findByEmail(email.toLowerCase());
            if (account == null) {
                continue;
            }
            if (UserRole.SUPER_ADMIN.getCode().equals(account.getRole())) {
                continue;
            }
            account.setRole(UserRole.SUPER_ADMIN.getCode());
            authDomainService.save(account);
            log.info("bootstrap super admin email={}", email);
        }
    }
}
