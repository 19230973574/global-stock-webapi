package com.globalstock.webapi.service.domain;

import com.globalstock.webapi.infra.AuthInfra;
import com.globalstock.webapi.model.document.UserAccountDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 认证领域服务。
 *
 * <p>职责说明：提供账号查询、创建、激活码和重置令牌更新能力。</p>
 *
 * @author Global Stock Team
 * @since 0.0.1
 */
@Service
public class AuthDomainService {

    private static final Logger log = LoggerFactory.getLogger(AuthDomainService.class);

    private final AuthInfra authInfra;

    public AuthDomainService(AuthInfra authInfra) {
        this.authInfra = authInfra;
    }

    /**
     * 按邮箱查询账号。
     *
     * @param email 邮箱
     * @return 账号文档
     */
    public UserAccountDocument findByEmail(String email) {
        UserAccountDocument document = authInfra.findByEmail(email);
        if (document == null) {
            log.warn("user account not found email={}", email);
        }
        return document;
    }

    /**
     * 创建账号。
     *
     * @param name 姓名
     * @param email 邮箱
     * @param passwordHash 密码哈希
     * @param activationCode 激活码
     * @param activationExpiresAt 激活过期时间
     * @return 账号文档
     */
    public UserAccountDocument createAccount(String name, String email, String passwordHash,
                                             String activationCode, long activationExpiresAt,
                                             String role) {
        long now = System.currentTimeMillis();
        UserAccountDocument document = new UserAccountDocument();
        document.setName(name);
        document.setEmail(email);
        document.setPasswordHash(passwordHash);
        document.setStatus("pending_activation");
        document.setRole(role);
        document.setActivationCode(activationCode);
        document.setActivationExpiresAt(activationExpiresAt);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return authInfra.save(document);
    }

    public java.util.List<UserAccountDocument> findAll() {
        return authInfra.findAll();
    }

    public long countAll() {
        return authInfra.countAll();
    }

    public long countActive() {
        return authInfra.countActive();
    }

    public boolean existsByRole(String role) {
        return authInfra.existsByRole(role);
    }

    /**
     * 保存账号。
     *
     * @param document 账号文档
     * @return 保存后的账号文档
     */
    public UserAccountDocument save(UserAccountDocument document) {
        document.setUpdatedAt(System.currentTimeMillis());
        return authInfra.save(document);
    }
}
